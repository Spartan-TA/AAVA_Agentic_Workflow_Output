-- V1__init.sql: Initial schema for Warehouse Employee Management System
CREATE TABLE employee (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    badge_id VARCHAR(50) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL,
    department VARCHAR(100) NOT NULL,
    shift_group VARCHAR(50),
    hire_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add additional tables for RBAC, attendance, shift, leave, certification, asset, etc. in subsequent migrations.
