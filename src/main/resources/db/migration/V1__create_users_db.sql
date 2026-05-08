CREATE TABLE IF NOT EXISTS users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL,
    password    VARCHAR(100)
);

CREATE UNIQUE INDEX idx_user_e ON users(email);

INSERT INTO users(id, first_name, last_name, email, password)
VALUES
    (1, 'Yury', 'Shakhau', 'shakhauyury@gmail.com', null);