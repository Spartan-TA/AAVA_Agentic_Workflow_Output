-- V2__employee_tables.sql
-- Employee table for Warehouse Employee Management System

CREATE TABLE IF NOT EXISTS employee (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    badge_id VARCHAR(50) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL,
    department VARCHAR(100) NOT NULL,
    shift_group VARCHAR(100),
    hire_date DATE,
    status VARCHAR(50) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);
