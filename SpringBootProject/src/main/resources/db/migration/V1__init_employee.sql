-- Baseline migration for Employee table
CREATE TABLE employees (
    id UUID PRIMARY KEY,
    badge_id VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL,
    department VARCHAR(50),
    shift_group VARCHAR(50),
    hire_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_employees_badge_id ON employees(badge_id);
