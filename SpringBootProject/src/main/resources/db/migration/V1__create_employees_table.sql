CREATE TABLE employees (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    badge_id VARCHAR(64) NOT NULL UNIQUE,
    role VARCHAR(64) NOT NULL,
    department VARCHAR(64) NOT NULL,
    shift_group VARCHAR(64),
    hire_date DATE,
    status VARCHAR(32),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);
