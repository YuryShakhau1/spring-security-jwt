package by.shakhau.strpingsecurityjwt.service.impl;

import by.shakhau.strpingsecurityjwt.domain.model.Role;
import by.shakhau.strpingsecurityjwt.domain.model.User;
import by.shakhau.strpingsecurityjwt.domain.repository.UserRepository;
import by.shakhau.strpingsecurityjwt.domain.repository.UserSpecifications;
import by.shakhau.strpingsecurityjwt.service.RefreshTokenService;
import by.shakhau.strpingsecurityjwt.service.UserRoleService;
import by.shakhau.strpingsecurityjwt.service.UserService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static by.shakhau.strpingsecurityjwt.domain.model.RoleType.ADMIN;
import static by.shakhau.strpingsecurityjwt.domain.model.RoleType.SUPER_ADMIN;

@Service
@Transactional
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final UserRoleService userRoleService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    @Override
    public Page<User> searchUsers(String firstName, String lastName, String email, Pageable pageable) {
        return repository.findAll(UserSpecifications.search(firstName, lastName, email), pageable);
    }

    @Override
    public User findById(Long userId) {
        return repository.findById(userId).orElse(null);
    }

    @Override
    public User findByEmail(String email) {
        return repository.findByEmail(email).orElse(null);
    }

    @Override
    public User createUser(User user, char[] password) {
        if (repository.findByEmail(user.getEmail()).isPresent()) {
            return null;
        }

        user.setId(null);
        user.setPassword(passwordEncoder.encode(new StringBuilder().append(password)));
        User createdUser = repository.save(user);
        userRoleService.createDefaultRole(createdUser.getId());
        return createdUser;
    }

    @Override
    public User updateUser(Long actorId, User user) {
        Role actorRole = userRoleService.highestPriorityRole(actorId);
        if (!actorId.equals(user.getId())) {
            if (actorRole.getPriority() > ADMIN.getPriority()) {
                return null;
            }

            if (!SUPER_ADMIN.getPriority().equals(actorRole.getPriority())
                    && userRoleService.hasRole(user.getId(), SUPER_ADMIN)) {
                return null;
            }
        }

        User userFound = repository.findById(user.getId()).orElse(null);
        if (userFound == null) {
            return null;
        }

        userFound.setFirstName(user.getFirstName());
        userFound.setLastName(user.getLastName());
        userFound.setEmail(user.getEmail());
        return repository.save(userFound);
    }

    @Override
    public boolean updatePassword(String email, char[] password) {
        User user = findByEmail(email);
        if (user.getPassword() == null) {
            boolean superAdmin = userRoleService.findByUserId(user.getId()).stream()
                    .anyMatch(ur -> SUPER_ADMIN.getName().equals(ur.getRole().getName()));
            if (superAdmin) {
                user.setPassword(passwordEncoder.encode(new StringBuilder().append(password)));
                repository.save(user);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean updatePassword(Long userId, char[] oldPassword, char[] newPassword) {
        User user = findById(userId);
        if (user.getPassword().equals(passwordEncoder.encode(new StringBuilder().append(oldPassword)))) {
            user.setPassword(passwordEncoder.encode(new StringBuilder().append(newPassword)));
            repository.save(user);
            return true;
        }

        return false;
    }

    @Override
    public void deleteById(Long actorId, Long targetUserId) {
        Role actorRole = userRoleService.highestPriorityRole(actorId);
        if (actorId.equals(targetUserId)) {
            if (SUPER_ADMIN.getPriority().equals(actorRole.getPriority())) {
                return;
            }

            deleteById(targetUserId);
            return;
        }

        Role targetUserRole = userRoleService.highestPriorityRole(targetUserId);
        if (actorRole.getPriority() <= targetUserRole.getPriority()) {
            deleteById(targetUserId);
        }
    }

    private void deleteById(Long targetUserId) {
        refreshTokenService.deleteByUserId(targetUserId);
        userRoleService.deleteByUserId(targetUserId);
        repository.deleteById(targetUserId);
    }
}
