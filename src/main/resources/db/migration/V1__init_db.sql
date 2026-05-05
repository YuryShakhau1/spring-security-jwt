CREATE TABLE IF NOT EXISTS users (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    email      VARCHAR(256) NOT NULL,
    password   VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id             BIGINT(20) NOT NULL,
    session_id          VARCHAR(256) NOT NULL,
    refresh_token       VARCHAR(256) NOT NULL,
    created_at          TIMESTAMP NOT NULL,
    expired_at          TIMESTAMP NOT NULL
);

CREATE INDEX idx_r_t_u_id ON refresh_tokens(user_id);
CREATE INDEX idx_u_u_n ON users(name);