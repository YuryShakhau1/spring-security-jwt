package by.shakhau.strpingsecurityjwt.controller;

import by.shakhau.strpingsecurityjwt.domain.model.RefreshTokenEntity;
import by.shakhau.strpingsecurityjwt.domain.model.User;
import by.shakhau.strpingsecurityjwt.security.JwtConfig;
import by.shakhau.strpingsecurityjwt.security.JwtService;
import by.shakhau.strpingsecurityjwt.service.RefreshTokenService;
import by.shakhau.strpingsecurityjwt.service.UserService;
import by.shakhau.strpingsecurityjwt.util.HashUtils;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtConfig jwtConfig;

    @AllArgsConstructor
    @Getter
    public static class RegisterRequest {
        public String userName;
        public String userLastName;
        public String email;
        public char[] password;
    }

    @AllArgsConstructor
    @Getter
    public static class LoginRequest {
        public String email;
        public char[] password;
    }

    @AllArgsConstructor
    @Getter
    public static class TokenResponse {
        private String accessToken;
        private String refreshToken;
    }

    @AllArgsConstructor
    @Getter
    public static class RefreshTokenRequest {
        public String refreshToken;
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> register(@RequestBody RegisterRequest request) {
        return Mono.fromCallable(() -> {
                    if (userService.createUser(new User(
                            null,
                            request.getUserName(),
                            request.getUserLastName(),
                            request.getEmail(),
                            null), request.getPassword()) == null) {
                        Arrays.fill(request.getPassword(), '0');
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User already exists");
                    }

                    Arrays.fill(request.getPassword(), '0');
                    return ResponseEntity.status(HttpStatus.OK).<String>body(null);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<TokenResponse>> login(@RequestBody LoginRequest request) {
        return Mono.fromCallable(() -> {
                    User user = userService.findByEmail(request.getEmail());
                    var password = new StringBuilder().append(request.getPassword());
                    Arrays.fill(request.getPassword(), '0');
                    if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
                        password.delete(0, password.length());
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).<TokenResponse>body(null);
                    }

                    password.delete(0, password.length());
                    Long userId = user.getId();
                    String accessToken = jwtService.generateAccessToken(user.getId(), Collections.emptyList());
                    String refreshToken = jwtService.generateRefreshToken(user.getId(), null, Collections.emptyList());

                    refreshTokenService.save(userId, refreshToken);

                    deleteSessionOutOfLimit(userId);

                    return ResponseEntity.ok(new TokenResponse(accessToken, refreshToken));
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<TokenResponse>> refreshAccessToken(@RequestBody RefreshTokenRequest request) {
        return Mono.fromCallable(() -> {
                    if (!jwtService.isTokenValid(request.getRefreshToken())) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).<TokenResponse>body(null);
                    }

                    var refreshTokenHash = HashUtils.sha256(request.getRefreshToken());
                    Claims claims = jwtService.getClaims(request.getRefreshToken());
                    Long userId = Long.valueOf(claims.getSubject());
                    String sessionId = (String) claims.get("session_id");
                    RefreshTokenEntity existingToken = refreshTokenService.findByUserIdAndSessionId(userId, sessionId);
                    if (existingToken == null || !refreshTokenHash.equals(existingToken.getRefreshTokenHash())) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).<TokenResponse>body(null);
                    }

                    String accessToken = jwtService.generateAccessToken(userId, Collections.emptyList());
                    String refreshToken = jwtService.generateRefreshToken(userId, sessionId, Collections.emptyList());
                    existingToken.setRefreshTokenHash(HashUtils.sha256(refreshToken));
                    existingToken.setExpiredAt(jwtService.getClaims(refreshToken).getExpiration());
                    refreshTokenService.save(existingToken);
                    return ResponseEntity.ok(new TokenResponse(accessToken, refreshToken));
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @DeleteMapping("/logout")
    public Mono<ResponseEntity<String>> logout(@RequestHeader("Authorization") String authHeader) {
        return Mono.fromCallable(() -> {
                    var bearer = "Bearer ";
                    if (authHeader == null || !authHeader.startsWith(bearer)) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Authorization header is missing or invalid");
                    }

                    String accessToken = authHeader.substring(bearer.length());
                    Claims claims = jwtService.getClaims(accessToken);
                    Long userId = Long.valueOf(claims.getSubject());
                    String sessionId = (String) claims.get("session_id");
                    refreshTokenService.deleteByUserIdAndSessionId(userId, sessionId);
                    return ResponseEntity.status(HttpStatus.OK).body("Logout is successful");
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @DeleteMapping("/logout/all")
    public Mono<ResponseEntity<String>> logoutAllSession(@RequestHeader("Authorization") String authHeader) {
        return Mono.fromCallable(() -> {
                    var bearer = "Bearer ";
                    if (authHeader == null || !authHeader.startsWith(bearer)) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Authorization header is missing or invalid");
                    }

                    String accessToken = authHeader.substring(bearer.length());
                    Claims claims = jwtService.getClaims(accessToken);
                    Long userId = Long.valueOf(claims.getSubject());
                    refreshTokenService.deleteByUserId(userId);
                    return ResponseEntity.status(HttpStatus.OK).body("Logout of all sessions is successful");
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    private void deleteSessionOutOfLimit(Long userId) {
        long sessionCount = refreshTokenService.countByUserId(userId);
        int maxSessionCount = jwtConfig.getMaxSessionCount();
        int validTokensCount = 0;
        if (sessionCount > maxSessionCount) {
            List<RefreshTokenEntity> tokens = refreshTokenService.findByUserId(userId);
            if (tokens.size() > maxSessionCount) {
                var dateNow = new Date();
                var tokensToDelete = new ArrayList<RefreshTokenEntity>();
                tokens.sort(Comparator.comparing(RefreshTokenEntity::getCreatedAt).reversed());
                for (RefreshTokenEntity token : tokens) {
                    if (validTokensCount < maxSessionCount && token.getExpiredAt().after(dateNow)) {
                        validTokensCount++;
                    } else {
                        tokensToDelete.add(token);
                    }
                }

                refreshTokenService.deleteAll(tokensToDelete);
            }
        }
    }
}
