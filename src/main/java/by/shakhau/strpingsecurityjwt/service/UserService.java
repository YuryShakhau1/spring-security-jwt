package by.shakhau.strpingsecurityjwt.service;

import by.shakhau.strpingsecurityjwt.domain.model.User;

import java.util.Collection;

public interface UserService {

    Collection<User> findAll();
    User findById(Long userId);
    User findByName(String userName);
    User createUser(User user, char[] password);
    User updateUser(User user);
    void deleteByName(Long userId);
}
