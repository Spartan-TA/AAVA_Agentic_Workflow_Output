-- Flyway Baseline Migration for Warehouse EMS
-- Creates initial tables for employee, attendance, scheduling, safety modules

CREATE TABLE employee (
    id BIGSERIAL PRIMARY KEY,
    badge_id VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    role VARCHAR(32) NOT NULL,
    department VARCHAR(64),
    shift_group VARCHAR(32),
    hire_date DATE,
    status VARCHAR(16) NOT NULL,
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE attendance_event (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    event_type VARCHAR(16) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    device_id VARCHAR(64),
    location VARCHAR(128),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE shift_template (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    recurrence VARCHAR(32),
    warehouse_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE shift_assignment (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    shift_template_id BIGINT NOT NULL REFERENCES shift_template(id),
    assignment_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE safety_incident (
    id BIGSERIAL PRIMARY KEY,
    severity VARCHAR(16) NOT NULL,
    location VARCHAR(128),
    description TEXT,
    status VARCHAR(16) NOT NULL,
    reported_by BIGINT REFERENCES employee(id),
    reported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
