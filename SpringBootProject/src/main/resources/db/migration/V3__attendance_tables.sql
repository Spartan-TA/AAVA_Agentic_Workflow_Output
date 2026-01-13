-- V3__attendance_tables.sql
-- AttendanceEvent table for Warehouse Employee Management System

CREATE TABLE IF NOT EXISTS attendance_event (
    id SERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    type VARCHAR(20) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    location VARCHAR(255),
    device_id VARCHAR(100),
    status VARCHAR(50)
);
