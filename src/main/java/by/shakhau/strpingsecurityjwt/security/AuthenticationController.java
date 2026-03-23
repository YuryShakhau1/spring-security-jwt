package by.shakhau.strpingsecurityjwt.security;

import by.shakhau.strpingsecurityjwt.dto.User;
import by.shakhau.strpingsecurityjwt.service.UserCredentialsService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Optional;

@AllArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private JwtService jwtService;
    private UserCredentialsService userCredentialsService;

    @AllArgsConstructor
    @Getter
    public static class AuthRequest {
        public String userName;
        public String password;
    }

    @AllArgsConstructor
    @Getter
    public static class AuthResponse {
        public String token;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@RequestBody AuthRequest request) {
        User user = userCredentialsService.findByName(request.getUserName());
        if (user == null || !request.getPassword().equals(user.getPassword())) {
            return Mono.just(ResponseEntity.status(401).build());
        }

        String token = jwtService.generateToken(user.getName(), user.getId());
        return Mono.just(ResponseEntity.ok(new AuthResponse(token)));
    }
}
