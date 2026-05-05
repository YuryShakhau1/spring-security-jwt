package by.shakhau.strpingsecurityjwt.service;

import by.shakhau.strpingsecurityjwt.domain.model.RefreshTokenEntity;

public interface RefreshTokenService {

    RefreshTokenEntity findByUserIdAndSessionId(Long userId, String sessionId);
    void save(Long userId, String refreshToken);
    void deleteByUserIdAndSessionId(Long userId, String sessionId);
    void deleteByUserId(Long userId);
}
