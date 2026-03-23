package by.shakhau.strpingsecurityjwt.service.impl;

import by.shakhau.strpingsecurityjwt.dto.User;
import by.shakhau.strpingsecurityjwt.service.UserCredentialsService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserCredentialsServiceImpl implements UserCredentialsService {

    private AtomicLong maxUserId = new AtomicLong(0);
    private Map<String, User> userMap = new HashMap<>();

    @Override
    public Collection<User> findAll() {
        return userMap.values();
    }

    @Override
    public User findByName(String name) {
        return userMap.get(name);
    }

    @Override
    public User createUser(User user) {
        if (userMap.containsKey(user.getName())) {
            return userMap.get(user.getName());
        }
        user.setId(maxUserId.incrementAndGet());
        userMap.put(user.getName(), user);
        return user;
    }

    @Override
    public User updateUser(String userName, User user) {
        User userFound = userMap.get(userName);
        if (userFound == null) {
            return null;
        }
        user.setId(userFound.getId());
        userMap.put(user.getName(), user);
        userMap.remove(userName);
        return user;
    }

    @Override
    public void deleteByName(String name) {
        userMap.remove(name);
    }
}
