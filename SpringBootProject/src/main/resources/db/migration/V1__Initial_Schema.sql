-- V1__Initial_Schema.sql
CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    badge_id VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(64) NOT NULL,
    department VARCHAR(64) NOT NULL,
    shift_group VARCHAR(64),
    hire_date DATE,
    status VARCHAR(32) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);
-- Additional tables for users, roles, and other modules will be added in subsequent migrations.
