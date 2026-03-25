package by.shakhau.strpingsecurityjwt.security;

import by.shakhau.strpingsecurityjwt.domain.model.User;
import by.shakhau.strpingsecurityjwt.service.UserService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Collections;

@AllArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private JwtService jwtService;
    private UserService userService;

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
        User user = userService.findByName(request.getUserName());
        if (user == null || !request.getPassword().equals(user.getPassword())) {
            return Mono.just(ResponseEntity.status(401).build());
        }

        String token = jwtService.generateToken(user.getId(), Collections.emptyList());
        return Mono.just(ResponseEntity.ok(new AuthResponse(token)));
    }

    @PostMapping("/register")
    public User register(@RequestBody AuthRequest request) {
        return userService.createUser(new User(null, request.getUserName(), request.getPassword()));
    }
}
