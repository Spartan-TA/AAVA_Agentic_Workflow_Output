-- V1__init_employees.sql
CREATE TABLE employees (
    id SERIAL PRIMARY KEY,
    badge_id VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL,
    department VARCHAR(128) NOT NULL,
    shift_group VARCHAR(32) NOT NULL,
    hire_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);
