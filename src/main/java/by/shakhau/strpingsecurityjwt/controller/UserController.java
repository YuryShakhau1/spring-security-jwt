package by.shakhau.strpingsecurityjwt.controller;

import by.shakhau.strpingsecurityjwt.domain.model.User;
import by.shakhau.strpingsecurityjwt.security.UserPrincipal;
import by.shakhau.strpingsecurityjwt.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/user")
@AllArgsConstructor
public class UserController {

    private UserService userService;

    @GetMapping(value = "/", produces = APPLICATION_JSON_VALUE)
    public Collection<User> findAll() {
        return userService.findAll();
    }

    @GetMapping(value = "/{userId}", produces = APPLICATION_JSON_VALUE)
    public User findByName(@PathVariable Long userId) {
        return userService.findById(userId);
    }

    @PutMapping(value = "/", produces = APPLICATION_JSON_VALUE)
    public User updateUser(@RequestBody User user, Authentication authentication) {
        Long userId = user.getId();
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        if (!principal.getId().equals(userId)) {
            throw new BadCredentialsException("Wrong user with id = " + userId);
        }
        return userService.updateUser(user);
    }

    @DeleteMapping(value = "/{userId}", produces = APPLICATION_JSON_VALUE)
    public void updateUser(@PathVariable Long userId, Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        if (!principal.getId().equals(userId)) {
            throw new BadCredentialsException("Wrong user with id = " + userId);
        }
        userService.deleteByName(userId);
    }
}
