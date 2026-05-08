package by.shakhau.strpingsecurityjwt.service;

import by.shakhau.strpingsecurityjwt.domain.model.Role;
import by.shakhau.strpingsecurityjwt.domain.model.RoleType;
import by.shakhau.strpingsecurityjwt.domain.model.UserRole;

import java.util.List;

public interface UserRoleService {

    List<UserRole> findByUserId(Long userId);
    void createDefaultRole(Long userId);
    boolean updateUserRole(Long actorId, Long targetUserId, Long existingRoleId, Long expectedRoleId);
    boolean hasRole(Long userId, RoleType roleType);
    Role highestPriorityRole(Long userId);
    boolean deleteByUserId(Long actorId, Long targetUserId, Long roleId);
    void deleteByUserId(Long userId);
}
