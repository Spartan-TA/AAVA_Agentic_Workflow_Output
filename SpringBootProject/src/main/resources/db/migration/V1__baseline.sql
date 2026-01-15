CREATE TABLE employees (
    id SERIAL PRIMARY KEY,
    badge_id VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    role VARCHAR(32) NOT NULL,
    department VARCHAR(64) NOT NULL,
    shift_group VARCHAR(64),
    hire_date DATE,
    status VARCHAR(32) NOT NULL,
    soft_deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_employee_badge_id ON employees(badge_id);
CREATE INDEX idx_employee_department ON employees(department);

-- Additional tables for attendance, schedule, leave, certification, safety, asset, performance, payroll, notification, audit, etc.
-- These will be added in subsequent migrations as modules are implemented

CREATE TABLE attendance_events (
    id SERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    event_type VARCHAR(32) NOT NULL,
    event_timestamp TIMESTAMP NOT NULL,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    device_info VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_attendance_employee ON attendance_events(employee_id);
CREATE INDEX idx_attendance_timestamp ON attendance_events(event_timestamp);

CREATE TABLE audit_logs (
    id SERIAL PRIMARY KEY,
    action VARCHAR(64) NOT NULL,
    entity_type VARCHAR(64),
    entity_id BIGINT,
    actor VARCHAR(128),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    before_value TEXT,
    after_value TEXT,
    metadata JSONB
);

CREATE INDEX idx_audit_action ON audit_logs(action);
CREATE INDEX idx_audit_timestamp ON audit_logs(timestamp);
CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);