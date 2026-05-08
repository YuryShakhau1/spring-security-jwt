package by.shakhau.strpingsecurityjwt.service.impl;

import by.shakhau.strpingsecurityjwt.domain.model.Role;
import by.shakhau.strpingsecurityjwt.domain.model.RoleType;
import by.shakhau.strpingsecurityjwt.domain.model.UserRole;
import by.shakhau.strpingsecurityjwt.domain.repository.RoleRepository;
import by.shakhau.strpingsecurityjwt.domain.repository.UserRoleRepository;
import by.shakhau.strpingsecurityjwt.service.RefreshTokenService;
import by.shakhau.strpingsecurityjwt.service.UserRoleService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

import static by.shakhau.strpingsecurityjwt.domain.model.RoleType.SUPER_ADMIN;

@Service
@Transactional
@AllArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {

    private final RoleRepository roleRepository;
    private final RefreshTokenService refreshTokenService;
    private final UserRoleRepository repository;

    @Override
    public List<UserRole> findByUserId(Long userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public void createDefaultRole(Long userId) {
        repository.save(new UserRole(userId, new Role(RoleType.USER_ROLE_ID)));
    }

    @Override
    public boolean updateUserRole(Long actorId, Long targetUserId, Long existingRoleId, Long expectedRoleId) {
        if (hasRole(targetUserId, SUPER_ADMIN)) {
            return false;
        }

        Role expectedRole = roleRepository.findById(expectedRoleId).orElseThrow();
        UserRole targetUserRole = null;
        if (existingRoleId != null) {
            targetUserRole = repository.findByUserIdAndRoleId(targetUserId, existingRoleId).orElseThrow();
            targetUserRole.setRole(expectedRole);
            refreshTokenService.deleteByUserId(targetUserId);
        } else {
            targetUserRole = new UserRole(targetUserId, expectedRole);
        }

        repository.save(targetUserRole);
        return true;
    }

    @Override
    public boolean hasRole(Long userId, RoleType roleType) {
        return repository.findByUserId(userId).stream()
                .anyMatch(ur -> roleType.getPriority().equals(ur.getRole().getPriority()));
    }

    @Override
    public Role highestPriorityRole(Long userId) {
        return repository.findByUserId(userId).stream()
                .min(Comparator.comparing(ur -> ur.getRole().getPriority()))
                .map(UserRole::getRole)
                .orElseThrow();
    }

    @Override
    public boolean deleteByUserId(Long actorId, Long targetUserId, Long roleId) {
        if (hasRole(targetUserId, SUPER_ADMIN)) {
            return false;
        }

        repository.deleteByUserIdAndRoleId(targetUserId, roleId);
        return true;
    }

    @Override
    public void deleteByUserId(Long userId) {
        repository.deleteByUserId(userId);
    }
}


