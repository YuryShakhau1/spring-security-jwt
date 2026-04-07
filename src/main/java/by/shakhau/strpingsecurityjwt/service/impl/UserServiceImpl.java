package by.shakhau.strpingsecurityjwt.service.impl;

import by.shakhau.strpingsecurityjwt.domain.model.User;
import by.shakhau.strpingsecurityjwt.domain.repository.UserRepository;
import by.shakhau.strpingsecurityjwt.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;

@AllArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Collection<User> findAll() {
        return repository.findAll();
    }

    @Override
    public User findById(Long userId) {
        return repository.findById(userId).orElse(null);
    }

    @Override
    public User findByName(String userName) {
        return repository.findByName(userName).orElse(null);
    }

    @Override
    public User createUser(User user, char[] password) {
        if (repository.findByName(user.getName()).isPresent()) {
            return null;
        }

        user.setId(null);
        user.setPassword(passwordEncoder.encode(new StringBuilder().append(password)));
        return repository.save(user);
    }

    @Override
    public User updateUser(User user) {
        if (repository.findById(user.getId()).isEmpty()) {
            return null;
        }

        return repository.save(user);
    }

    @Override
    public void deleteByName(Long userId) {
        repository.deleteById(userId);
    }
}
