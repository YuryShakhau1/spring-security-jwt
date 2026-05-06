package by.shakhau.strpingsecurityjwt.service;

import by.shakhau.strpingsecurityjwt.domain.model.RefreshTokenEntity;

import java.util.Collection;
import java.util.List;

public interface RefreshTokenService {

    RefreshTokenEntity findByUserIdAndSessionId(Long userId, String sessionId);
    List<RefreshTokenEntity> findByUserId(Long userId);
    long countByUserId(Long userId);
    void save(Long userId, String refreshToken);
    void save(RefreshTokenEntity refreshToken);
    void deleteByUserIdAndSessionId(Long userId, String sessionId);
    void deleteByUserId(Long userId);
    void deleteAll(Collection<RefreshTokenEntity> toDelete);
}
