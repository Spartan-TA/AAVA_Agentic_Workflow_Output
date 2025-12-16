INSERT INTO roles (name) VALUES ('ADMIN'), ('HR'), ('SUPERVISOR'), ('WORKER');

INSERT INTO users (username, password) VALUES ('admin', '$2a$10$7QJwQwQwQwQwQwQwQwQwQwQwQwQwQwQwQwQwQwQwQwQwQwQwQwQwQwQwQw'); -- bcrypt hash for 'admin123'

INSERT INTO user_roles (user_id, role_id) VALUES (1, 1); -- Assign ADMIN role to admin user