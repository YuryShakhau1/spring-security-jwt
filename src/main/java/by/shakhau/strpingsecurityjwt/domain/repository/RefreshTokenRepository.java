package by.shakhau.strpingsecurityjwt.domain.repository;

import by.shakhau.strpingsecurityjwt.domain.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByUserIdAndSessionId(Long userId, String sessionId);
    List<RefreshToken> findByUserId(Long userId);
    long countByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM RefreshToken WHERE userId = :userId AND sessionId = :sessionId")
    void deleteByUserIdAndSessionId(Long userId, String sessionId);

    @Modifying
    @Query("DELETE FROM RefreshToken WHERE userId = :userId")
    void deleteByUserId(Long userId);
}
