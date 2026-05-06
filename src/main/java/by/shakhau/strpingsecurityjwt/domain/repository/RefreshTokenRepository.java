package by.shakhau.strpingsecurityjwt.domain.repository;

import by.shakhau.strpingsecurityjwt.domain.model.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    Optional<RefreshTokenEntity> findByUserIdAndSessionId(Long userId, String sessionId);
    List<RefreshTokenEntity> findByUserId(Long userId);
    long countByUserId(Long userId);
    void deleteByUserIdAndSessionId(Long userId, String sessionId);
    void deleteByUserId(Long userId);
}
