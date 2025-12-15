-- V1__initial_schema.sql
-- Initial schema for Warehouse EMS

CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    badge_id VARCHAR(20) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL,
    department VARCHAR(50) NOT NULL,
    shift_group VARCHAR(50),
    hire_date DATE,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Add additional tables for other modules in future migrations
