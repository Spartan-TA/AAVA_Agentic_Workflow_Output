-- V4__scheduling_tables.sql
-- Scheduling tables for Warehouse Employee Management System

CREATE TABLE IF NOT EXISTS shift_template (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    recurrence VARCHAR(50) NOT NULL,
    overtime_rule VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS shift_assignment (
    id SERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    shift_template_id BIGINT NOT NULL REFERENCES shift_template(id),
    date DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS blackout_date (
    id SERIAL PRIMARY KEY,
    date DATE NOT NULL UNIQUE,
    reason VARCHAR(255)
);
