package by.shakhau.strpingsecurityjwt.service;

import by.shakhau.strpingsecurityjwt.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    Page<User> searchUsers(String firstName, String lastName, String email, Pageable pageable);
    User findById(Long userId);
    User findByEmail(String email);
    User createUser(User user, char[] password);
    User updateUser(Long actorId, User user);
    boolean updatePassword(String email, char[] password);
    boolean updatePassword(Long userId, char[] oldPassword, char[] newPassword);
    void deleteById(Long actorId, Long targetUserId);
}
