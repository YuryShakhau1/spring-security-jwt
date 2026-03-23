package by.shakhau.strpingsecurityjwt.constroller;

import by.shakhau.strpingsecurityjwt.dto.User;
import by.shakhau.strpingsecurityjwt.service.UserCredentialsService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/user")
@AllArgsConstructor
public class UserController {

    private UserCredentialsService userCredentialsService;

    @GetMapping(value = "/", produces = APPLICATION_JSON_VALUE)
    public Collection<User> findAll() {
        return userCredentialsService.findAll();
    }

    @GetMapping(value = "/{userName}", produces = APPLICATION_JSON_VALUE)
    public User findByName(String userName) {
        return userCredentialsService.findByName(userName);
    }

    @PostMapping(value = "/", produces = APPLICATION_JSON_VALUE)
    public User createUser(@RequestBody User user) {
        return userCredentialsService.createUser(user);
    }

    @PutMapping(value = "/{userName}", produces = APPLICATION_JSON_VALUE)
    public User updateUser(
            @PathVariable String userName,
            @RequestBody User user,
            Authentication authentication) {
        String authUserName = (String) authentication.getPrincipal();
        if (!userName.equals(authUserName)) {
            throw new BadCredentialsException("Wrong user " + userName);
        }
        return userCredentialsService.updateUser(userName, user);
    }

    @DeleteMapping(value = "/{userName}", produces = APPLICATION_JSON_VALUE)
    public void updateUser(@PathVariable String userName, Authentication authentication) {
        String authUserName = (String) authentication.getPrincipal();
        if (!userName.equals(authUserName)) {
            throw new BadCredentialsException("Wrong user " + userName);
        }
        userCredentialsService.deleteByName(userName);
    }
}
