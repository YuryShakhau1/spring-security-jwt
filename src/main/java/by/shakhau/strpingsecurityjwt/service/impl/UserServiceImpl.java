package by.shakhau.strpingsecurityjwt.service.impl;

import by.shakhau.strpingsecurityjwt.domain.model.User;
import by.shakhau.strpingsecurityjwt.domain.repository.UserRepository;
import by.shakhau.strpingsecurityjwt.service.UserService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;

    @Override
    public Collection<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    @Override
    public User findByName(String userName) {
        return userRepository.findByName(userName).orElse(null);
    }

    @Override
    public User createUser(User user) {
        if (userRepository.findByName(user.getName()).isPresent()) {
            return null;
        }

        user.setId(null);
        return userRepository.save(user);
    }

    @Override
    public User updateUser(User user) {
        if (userRepository.findById(user.getId()).isEmpty()) {
            return null;
        }

        return userRepository.save(user);
    }

    @Override
    public void deleteByName(Long userId) {
        userRepository.deleteById(userId);
    }
}
