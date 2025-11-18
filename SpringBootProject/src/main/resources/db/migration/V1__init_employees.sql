CREATE TABLE employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    badge_id VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    role VARCHAR(64),
    department VARCHAR(64),
    shift_group VARCHAR(64),
    hire_date DATE,
    status VARCHAR(32),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);
