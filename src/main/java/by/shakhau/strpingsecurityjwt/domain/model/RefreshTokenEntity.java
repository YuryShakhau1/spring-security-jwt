package by.shakhau.strpingsecurityjwt.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity(name = "refresh_tokens")
@Getter
@Setter
@EqualsAndHashCode
public class RefreshTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String sessionId;
    private String refreshToken;
    private Date createdAt;
    private Date expiredAt;
}
