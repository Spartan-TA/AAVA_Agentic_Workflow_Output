-- V1__initial_schema.sql
-- Initial schema for Warehouse EMS
CREATE TABLE employees (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    badge_id VARCHAR(50) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL,
    department VARCHAR(100) NOT NULL,
    shift_group VARCHAR(50),
    hire_date DATE,
    status VARCHAR(50) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);
