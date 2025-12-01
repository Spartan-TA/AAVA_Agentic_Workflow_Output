-- Flyway migration: Initial schema for Employee table
CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    badge_id VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    role VARCHAR(32) NOT NULL,
    department VARCHAR(64) NOT NULL,
    shift_group VARCHAR(32),
    hire_date DATE NOT NULL,
    status VARCHAR(16) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);
