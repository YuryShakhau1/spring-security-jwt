package by.shakhau.strpingsecurityjwt.controller;

import by.shakhau.strpingsecurityjwt.domain.model.User;
import by.shakhau.strpingsecurityjwt.security.UserPrincipal;
import by.shakhau.strpingsecurityjwt.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/user")
@AllArgsConstructor
public class UserController {

    private UserService userService;

    @GetMapping(value = "/", produces = APPLICATION_JSON_VALUE)
    public Flux<User> findAll() {
        return Flux.fromIterable(userService.findAll());
    }

    @GetMapping(value = "/{userId}", produces = APPLICATION_JSON_VALUE)
    public Mono<User> findByName(@PathVariable Long userId) {
        return Mono.just(userService.findById(userId));
    }

    @PutMapping(value = "/", produces = APPLICATION_JSON_VALUE)
    public Mono<User> updateUser(@RequestBody User user, Authentication authentication) {
        return Mono.fromSupplier(() -> {
                    Long userId = user.getId();
                    UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
                    if (!principal.getId().equals(userId)) {
                        throw new BadCredentialsException("Wrong user with id = " + userId);
                    }
                    return userService.updateUser(user);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @DeleteMapping(value = "/{userId}", produces = APPLICATION_JSON_VALUE)
    public Mono<Void> updateUser(@PathVariable Long userId, Authentication authentication) {
        return Mono.fromRunnable(() -> {
                    UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
                    if (!principal.getId().equals(userId)) {
                        throw new BadCredentialsException("Wrong user with id = " + userId);
                    }
                    userService.deleteByName(userId);
                })
                .subscribeOn(Schedulers.boundedElastic()).then();
    }
}
