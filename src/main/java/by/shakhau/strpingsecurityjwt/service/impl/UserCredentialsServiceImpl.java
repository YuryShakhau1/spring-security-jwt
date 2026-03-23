package by.shakhau.strpingsecurityjwt.service.impl;

import by.shakhau.strpingsecurityjwt.dto.User;
import by.shakhau.strpingsecurityjwt.service.UserCredentialsService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserCredentialsServiceImpl implements UserCredentialsService {

    private AtomicLong maxUserId = new AtomicLong(0);
    private Map<Long, User> userMap = new HashMap<>();

    @Override
    public Collection<User> findAll() {
        return userMap.values();
    }

    @Override
    public User findById(Long userId) {
        return userMap.get(userId);
    }

    @Override
    public User findByName(String userName) {
        return userMap.values().stream()
                .filter(u -> userName.equals(u.getName()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public User createUser(User user) {
        if (findByName(user.getName()) != null) {
            return null;
        }

        user.setId(maxUserId.incrementAndGet());
        userMap.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        User userFound = userMap.get(user.getId());
        if (userFound == null) {
            return null;
        }

        user.setId(userFound.getId());
        userMap.put(user.getId(), user);
        userMap.remove(user.getId());
        return user;
    }

    @Override
    public void deleteByName(Long userId) {
        userMap.remove(userId);
    }
}
