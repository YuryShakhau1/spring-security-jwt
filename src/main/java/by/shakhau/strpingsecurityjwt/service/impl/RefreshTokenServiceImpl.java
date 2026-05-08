package by.shakhau.strpingsecurityjwt.service.impl;

import by.shakhau.strpingsecurityjwt.domain.model.RefreshToken;
import by.shakhau.strpingsecurityjwt.domain.repository.RefreshTokenRepository;
import by.shakhau.strpingsecurityjwt.security.JwtService;
import by.shakhau.strpingsecurityjwt.service.RefreshTokenService;
import by.shakhau.strpingsecurityjwt.util.HashUtils;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final JwtService jwtService;
    private final RefreshTokenRepository repository;

    @Override
    public RefreshToken findByUserIdAndSessionId(Long userId, String sessionId) {
        return repository.findByUserIdAndSessionId(userId, sessionId).orElse(null);
    }

    @Override
    public List<RefreshToken> findByUserId(Long userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public long countByUserId(Long userId) {
        return repository.countByUserId(userId);
    }

    @Override
    public void save(Long userId, String refreshToken) {
        Claims refreshTokenClaims = jwtService.getClaims(refreshToken);
        String sessionId = (String) refreshTokenClaims.get("session_id");
        Date expiredAt = refreshTokenClaims.getExpiration();

        RefreshToken token = new RefreshToken();
        token.setUserId(userId);
        token.setSessionId(sessionId);
        token.setCreatedAt(new Date());
        token.setExpiredAt(expiredAt);
        token.setRefreshTokenHash(HashUtils.sha256(refreshToken));
        repository.save(token);
    }

    @Override
    public void save(RefreshToken refreshToken) {
        repository.save(refreshToken);
    }

    @Override
    public void deleteByUserIdAndSessionId(Long userId, String sessionId) {
        repository.deleteByUserIdAndSessionId(userId, sessionId);
    }

    @Override
    public void deleteByUserId(Long userId) {
        repository.deleteByUserId(userId);
    }

    @Override
    public void deleteAll(Collection<RefreshToken> toDelete) {
        repository.deleteAll(toDelete);
    }
}
