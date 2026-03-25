package by.shakhau.strpingsecurityjwt.service.impl;

import by.shakhau.strpingsecurityjwt.domain.model.RefreshTokenEntity;
import by.shakhau.strpingsecurityjwt.domain.repository.RefreshTokenRepository;
import by.shakhau.strpingsecurityjwt.security.JwtService;
import by.shakhau.strpingsecurityjwt.service.RefreshTokenService;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@AllArgsConstructor
@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final JwtService jwtService;
    private final RefreshTokenRepository repository;

    @Override
    public boolean existsByUserIdAndSessionId(Long userId, String sessionId) {
        return repository.existsByUserIdAndSessionId(userId, sessionId);
    }

    @Override
    public void save(Long userId, String refreshToken) {
        Claims refreshTokenClaims = jwtService.getClaims(refreshToken);
        String sessionId = (String) refreshTokenClaims.get("session_id");
        Date expiredAt = refreshTokenClaims.getExpiration();

        RefreshTokenEntity token = new RefreshTokenEntity();
        token.setUserId(userId);
        token.setSessionId(sessionId);
        token.setCreatedAt(new Date());
        token.setExpiredAt(expiredAt);
        token.setRefreshToken(refreshToken);
        repository.save(token);
    }

    @Override
    public void deleteByUserIdAndSessionId(Long userId, String sessionId) {
        repository.deleteByUserIdAndSessionId(userId, sessionId);
    }

    @Transactional
    @Override
    public void deleteByUserId(Long userId) {
        repository.deleteByUserId(userId);
    }
}
