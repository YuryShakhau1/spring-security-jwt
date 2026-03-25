CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

-- Создание индекса на поле username (для быстрого поиска)
CREATE INDEX IF NOT EXISTS idx_users_username ON users(name);