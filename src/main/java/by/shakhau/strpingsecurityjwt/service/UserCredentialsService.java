package by.shakhau.strpingsecurityjwt.service;

import by.shakhau.strpingsecurityjwt.dto.User;

import java.util.Collection;

public interface UserCredentialsService {

    Collection<User> findAll();
    User findByName(String name);
    User createUser(User user);
    User updateUser(String userName, User user);
    void deleteByName(String name);
}
