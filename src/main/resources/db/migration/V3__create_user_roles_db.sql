CREATE TABLE IF NOT EXISTS user_roles (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT NOT NULL,
    role_id    BIGINT NOT NULL,
        CONSTRAINT uk_user_role UNIQUE (user_id, role_id)
);

CREATE INDEX idx_ur_role_id ON user_roles(role_id);

INSERT INTO user_roles(id, user_id, role_id)
VALUES
    (1, 1, 1);