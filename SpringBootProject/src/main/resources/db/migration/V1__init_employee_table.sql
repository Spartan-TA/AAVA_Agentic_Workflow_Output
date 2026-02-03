-- Flyway migration: Create employees table
CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    badge_id VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL,
    department VARCHAR(50) NOT NULL,
    shift_group VARCHAR(50),
    hire_date DATE,
    status VARCHAR(20) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);
