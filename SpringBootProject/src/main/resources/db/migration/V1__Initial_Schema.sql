-- V1__Initial_Schema.sql
-- Initial schema for Warehouse Employee Management System

CREATE TABLE employees (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    badge_id VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL,
    department VARCHAR(100) NOT NULL,
    shift_group VARCHAR(100) NOT NULL,
    hire_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);
