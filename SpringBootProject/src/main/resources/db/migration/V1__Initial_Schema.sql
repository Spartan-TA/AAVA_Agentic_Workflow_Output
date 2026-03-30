-- Flyway migration: Initial schema for Warehouse EMS
CREATE TABLE employees (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    badge_id VARCHAR(20) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);
-- Add indexes for badge_id and email
CREATE INDEX idx_employees_badge_id ON employees(badge_id);
CREATE INDEX idx_employees_email ON employees(email);
