-- Flyway migration for employee table
CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    badge_id VARCHAR(20) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL,
    department VARCHAR(50) NOT NULL,
    shift_group VARCHAR(50) NOT NULL,
    hire_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_employees_deleted ON employees(deleted);
