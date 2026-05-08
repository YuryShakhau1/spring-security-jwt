package by.shakhau.strpingsecurityjwt.service;

import by.shakhau.strpingsecurityjwt.domain.model.RefreshToken;

import java.util.Collection;
import java.util.List;

public interface RefreshTokenService {

    RefreshToken findByUserIdAndSessionId(Long userId, String sessionId);
    List<RefreshToken> findByUserId(Long userId);
    long countByUserId(Long userId);
    void save(Long userId, String refreshToken);
    void save(RefreshToken refreshToken);
    void deleteByUserIdAndSessionId(Long userId, String sessionId);
    void deleteByUserId(Long userId);
    void deleteAll(Collection<RefreshToken> toDelete);
}
