-- Flyway migration: Initial schema for Warehouse EMS

CREATE TABLE employees (
    id SERIAL PRIMARY KEY,
    badge_id VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    department VARCHAR(50) NOT NULL,
    shift_group VARCHAR(50),
    hire_date DATE,
    status VARCHAR(20) NOT NULL,
    warehouse_id BIGINT NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Add other tables for attendance, shifts, leave, etc. as needed for full implementation.