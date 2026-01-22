CREATE TABLE employee (
    id UUID PRIMARY KEY,
    badge_id VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    role VARCHAR(32) NOT NULL,
    department VARCHAR(64),
    shift_group VARCHAR(64),
    hire_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_employee_badge_id ON employee(badge_id);
CREATE INDEX idx_employee_deleted ON employee(deleted);
CREATE INDEX idx_employee_status ON employee(status);

-- Additional tables for attendance, shifts, leave, certifications, safety_incidents, assets, reviews, payroll_exports, notifications, integrations, audit_log, reports, tenants, etc.
-- These will be added in subsequent migration scripts as the system evolves