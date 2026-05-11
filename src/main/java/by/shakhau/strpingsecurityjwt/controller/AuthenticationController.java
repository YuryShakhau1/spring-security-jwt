package by.shakhau.strpingsecurityjwt.controller;

import by.shakhau.strpingsecurityjwt.domain.model.RefreshToken;
import by.shakhau.strpingsecurityjwt.domain.model.User;
import by.shakhau.strpingsecurityjwt.security.JwtConfig;
import by.shakhau.strpingsecurityjwt.security.JwtService;
import by.shakhau.strpingsecurityjwt.service.RefreshTokenService;
import by.shakhau.strpingsecurityjwt.service.UserRoleService;
import by.shakhau.strpingsecurityjwt.service.UserService;
import by.shakhau.strpingsecurityjwt.util.HashUtils;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@AllArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final JwtService jwtService;
    private final JwtConfig jwtConfig;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final UserRoleService userRoleService;
    private final UserService userService;

    @AllArgsConstructor
    @Getter
    public static class LoginRequest {
        public String email;
        public char[] password;
    }

    @AllArgsConstructor
    @Getter
    public static class RefreshTokenRequest {
        public String refreshToken;
    }

    @PostMapping(value = "/login", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> login(@RequestBody LoginRequest request, ServerHttpResponse response) {
        return Mono.fromCallable(() -> {
                    User user = userService.findByEmail(request.getEmail());
                    var password = new StringBuilder().append(request.getPassword());
                    Arrays.fill(request.getPassword(), '0');
                    if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
                        password.delete(0, password.length());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("");
                    }

                    password.delete(0, password.length());
                    Long userId = user.getId();
                    String accessToken = jwtService.generateAccessToken(user.getId(), userRoles(userId));
                    String refreshToken = jwtService.generateRefreshToken(user.getId(), null);

                    refreshTokenService.save(userId, refreshToken);

                    deleteSessionOutOfLimit(userId);

                    ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                            .httpOnly(true)
                            .secure(false)
                            .path("/")
                            .maxAge(jwtConfig.getRefreshExpiration())
                            .sameSite("Strict")
                            .build();
                    response.addCookie(refreshCookie);

                    return ResponseEntity.ok().body(accessToken);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @PostMapping(value = "/refresh", consumes = APPLICATION_JSON_VALUE)
    public Mono<Void> refreshAccessToken(@RequestBody RefreshTokenRequest request, ServerHttpResponse response) {
        return Mono.fromCallable(() -> {
                    if (!jwtService.isTokenValid(request.getRefreshToken())) {
                        return ServerResponse.status(HttpStatus.UNAUTHORIZED).build();
                    }

                    var refreshTokenHash = HashUtils.sha256(request.getRefreshToken());
                    Claims claims = jwtService.getClaims(request.getRefreshToken());
                    Long userId = Long.valueOf(claims.getSubject());
                    String sessionId = (String) claims.get("session_id");
                    RefreshToken existingToken = refreshTokenService.findByUserIdAndSessionId(userId, sessionId);
                    if (existingToken == null || !refreshTokenHash.equals(existingToken.getRefreshTokenHash())) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
                    }

                    String accessToken = jwtService.generateAccessToken(userId, userRoles(userId));
                    String refreshToken = jwtService.generateRefreshToken(userId, sessionId);
                    existingToken.setRefreshTokenHash(HashUtils.sha256(refreshToken));
                    existingToken.setExpiredAt(jwtService.getClaims(refreshToken).getExpiration());
                    refreshTokenService.save(existingToken);

                    ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                            .httpOnly(true)
                            .secure(false)
                            .path("/")
                            .maxAge(jwtConfig.getAccessExpiration())
                            .sameSite("Strict")
                            .build();
                    response.addCookie(accessCookie);

                    ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                            .httpOnly(true)
                            .secure(false)
                            .path("/")
                            .maxAge(jwtConfig.getRefreshExpiration())
                            .sameSite("Strict")
                            .build();
                    response.addCookie(refreshCookie);

                    return ServerResponse.ok().build();
                })
                .subscribeOn(Schedulers.boundedElastic()).then();
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
            List<RefreshToken> tokens = refreshTokenService.findByUserId(userId);
            if (tokens.size() > maxSessionCount) {
                var dateNow = new Date();
                var tokensToDelete = new ArrayList<RefreshToken>();
                tokens.sort(Comparator.comparing(RefreshToken::getCreatedAt).reversed());
                for (RefreshToken token : tokens) {
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

    private List<String> userRoles(Long userId) {
        return userRoleService.findByUserId(userId).stream()
                .map(ur -> ur.getRole().getName())
                .collect(Collectors.toList());
    }
}
