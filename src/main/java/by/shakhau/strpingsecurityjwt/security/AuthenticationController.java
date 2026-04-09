package by.shakhau.strpingsecurityjwt.security;

import by.shakhau.strpingsecurityjwt.domain.model.RefreshTokenEntity;
import by.shakhau.strpingsecurityjwt.domain.model.User;
import by.shakhau.strpingsecurityjwt.service.RefreshTokenService;
import by.shakhau.strpingsecurityjwt.service.UserService;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.Collections;

@AllArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @AllArgsConstructor
    @Getter
    public static class AuthRequest {
        public String userName;
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
    public Mono<ResponseEntity<String>> register(@RequestBody AuthRequest request) {
        if (userService.createUser(new User(null, request.getUserName(), null), request.getPassword()) == null) {
            Arrays.fill(request.getPassword(), '0');
            return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body("User already exists"));
        }

        Arrays.fill(request.getPassword(), '0');
        return Mono.just(ResponseEntity.status(HttpStatus.OK).build());
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<TokenResponse>> login(@RequestBody AuthRequest request) {
        User user = userService.findByName(request.getUserName());
        var password = new StringBuilder().append(request.getPassword());
        Arrays.fill(request.getPassword(), '0');
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            password.delete(0, password.length());
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        password.delete(0, password.length());
        Long userId = user.getId();
        String accessToken = jwtService.generateAccessToken(user.getId(), Collections.emptyList());
        String refreshToken = jwtService.generateRefreshToken(user.getId(), Collections.emptyList());

        refreshTokenService.save(userId, refreshToken);

        return Mono.just(ResponseEntity.ok(new TokenResponse(accessToken, refreshToken)));
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<TokenResponse>> refreshAccessToken(@RequestBody RefreshTokenRequest request) {
        return Mono.defer(() -> {
                    if (!jwtService.isTokenValid(request.getRefreshToken())) {
                        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .<TokenResponse>body(null));
                    }

                    Claims claims = jwtService.getClaims(request.getRefreshToken());
                    Long userId = Long.valueOf(claims.getSubject());
                    String sessionId = (String) claims.get("session_id");
                    RefreshTokenEntity existingToken = refreshTokenService.findByUserIdAndSessionId(userId, sessionId);
                    if (request.getRefreshToken().equals(existingToken.getRefreshToken())) {
                        String accessToken = jwtService.generateAccessToken(userId, Collections.emptyList());
                        String refreshToken = jwtService.generateRefreshToken(userId, Collections.emptyList());
                        existingToken.setRefreshToken(refreshToken);
                        refreshTokenService.save(userId, refreshToken);
                        return Mono.just(ResponseEntity.ok(new TokenResponse(accessToken, refreshToken)));
                    }

                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .<TokenResponse>body(null));
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @DeleteMapping("/logout")
    public Mono<ResponseEntity<String>> logout(@RequestHeader("Authorization") String authHeader) {
        return Mono.defer(() -> {
                    var bearer = "Bearer ";
                    if (authHeader != null && authHeader.startsWith(bearer)) {
                        String accessToken = authHeader.substring(bearer.length());
                        Claims claims = jwtService.getClaims(accessToken);
                        Long userId = Long.valueOf(claims.getSubject());
                        String sessionId = (String) claims.get("session_id");
                        refreshTokenService.deleteByUserIdAndSessionId(userId, sessionId);
                        return Mono.just(ResponseEntity.status(HttpStatus.OK).body("Logout is successful"));
                    }

                    return Mono.error(new RuntimeException("Authorization header is missing or invalid"));
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @DeleteMapping("/logout/all")
    public Mono<ResponseEntity<String>> logoutAllSession(@RequestHeader("Authorization") String authHeader) {
        return Mono.defer(() -> {
                    var bearer = "Bearer ";
                    if (authHeader != null && authHeader.startsWith(bearer)) {
                        String accessToken = authHeader.substring(bearer.length());
                        Claims claims = jwtService.getClaims(accessToken);
                        Long userId = Long.valueOf(claims.getSubject());
                        refreshTokenService.deleteByUserId(userId);
                        return Mono.just(ResponseEntity.status(HttpStatus.OK).body("Logout of all sessions is successful"));
                    }

                    return Mono.error(new RuntimeException("Authorization header is missing or invalid"));
                })
                .subscribeOn(Schedulers.boundedElastic());
    }
}
