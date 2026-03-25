package by.shakhau.strpingsecurityjwt.service;

import by.shakhau.strpingsecurityjwt.domain.model.RefreshTokenEntity;

import java.util.Date;

public interface RefreshTokenService {

    boolean existsByUserIdAndSessionId(Long userId, String sessionId);
    void save(Long userId, String refreshToken);
    void deleteByUserIdAndSessionId(Long userId, String sessionId);
    void deleteByUserId(Long userId);
}
