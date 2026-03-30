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
        public String password;
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
        if (userService.createUser(new User(null, request.getUserName(), request.getPassword())) == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body("User already exists"));
        }

        return Mono.just(ResponseEntity.status(HttpStatus.OK).build());
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<TokenResponse>> login(@RequestBody AuthRequest request) {
        User user = userService.findByName(request.getUserName());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        Long userId = user.getId();
        String accessToken = jwtService.generateAccessToken(user.getId(), Collections.emptyList());
        String refreshToken = jwtService.generateRefreshToken(user.getId(), Collections.emptyList());

        refreshTokenService.save(userId, refreshToken);

        return Mono.just(ResponseEntity.ok(new TokenResponse(accessToken, refreshToken)));
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<TokenResponse>> refreshAccessToken(@RequestBody RefreshTokenRequest request) {
        if (!jwtService.isTokenValid(request.getRefreshToken())) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
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

        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @DeleteMapping("/logout")
    public Mono<ResponseEntity<String>> logout(@RequestHeader("Authorization") String authHeader) {
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
    }

    @DeleteMapping("/logout/all")
    public Mono<ResponseEntity<String>> logoutAllSession(@RequestHeader("Authorization") String authHeader) {
        var bearer = "Bearer ";
        if (authHeader != null && authHeader.startsWith(bearer)) {
            String accessToken = authHeader.substring(bearer.length());
            Claims claims = jwtService.getClaims(accessToken);
            Long userId = Long.valueOf(claims.getSubject());
            refreshTokenService.deleteByUserId(userId);
            return Mono.just(ResponseEntity.status(HttpStatus.OK).body("Logout of all sessions is successful"));
        }

        return Mono.error(new RuntimeException("Authorization header is missing or invalid"));
    }
}
