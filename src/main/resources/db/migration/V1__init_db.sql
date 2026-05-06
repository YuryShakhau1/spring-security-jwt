CREATE TABLE IF NOT EXISTS users (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    email      VARCHAR(150) NOT NULL,
    password   VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT(20) NOT NULL,
    session_id          VARCHAR(256) NOT NULL,
    refresh_token_hash  VARCHAR(64) NOT NULL,
    created_at          TIMESTAMP NOT NULL,
    expired_at          TIMESTAMP NOT NULL
);


CREATE UNIQUE INDEX idx_user_e ON users(email);

CREATE INDEX idx_r_t_u_id ON refresh_tokens(user_id);
CREATE INDEX idx_r_t_c_a ON refresh_tokens(created_at);
CREATE INDEX idx_r_t_e_a ON refresh_tokens(expired_at);