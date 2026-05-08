CREATE TABLE IF NOT EXISTS roles (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    name      VARCHAR(25) NOT NULL,
    priority  INTEGER NOT NULL
);

CREATE INDEX idx_role_name ON roles(name);

INSERT INTO roles(id, name, priority)
VALUES
    (1, 'ROLE_SUPER_ADMIN', 1),
    (2, 'ROLE_ADMIN', 2),
    (3, 'ROLE_USER', 3);