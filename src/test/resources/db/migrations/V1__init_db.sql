CREATE TABLE IF NOT EXISTS users (
    id       BIGSERIAL PRIMARY KEY,
    name     VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE refresh_tokens (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    session_id      VARCHAR(255) NOT NULL,
    refresh_token   VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP NOT NULL,
    expired_at      TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_r_tokens_u_id ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(name);