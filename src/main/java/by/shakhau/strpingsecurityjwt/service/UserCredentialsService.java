package by.shakhau.strpingsecurityjwt.service;

import by.shakhau.strpingsecurityjwt.dto.User;

import java.util.Collection;

public interface UserCredentialsService {

    Collection<User> findAll();
    User findById(Long userId);
    User findByName(String userName);
    User createUser(User user);
    User updateUser(User user);
    void deleteByName(Long userId);
}
