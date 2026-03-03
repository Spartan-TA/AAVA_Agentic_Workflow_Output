-- Flyway Migration V1: Initial Schema
-- Creates base tables for Warehouse Employee Management System

-- Enable UUID extension if using PostgreSQL
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Employees table
CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    badge_id VARCHAR(32) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL,
    department VARCHAR(50),
    shift_group VARCHAR(50),
    hire_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    tenant_id VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for employees
CREATE INDEX idx_employees_badge_id ON employees(badge_id);
CREATE INDEX idx_employees_department ON employees(department);
CREATE INDEX idx_employees_deleted ON employees(deleted);
CREATE INDEX idx_employees_status ON employees(status);
CREATE INDEX idx_employees_tenant_id ON employees(tenant_id);

-- Shift templates table
CREATE TABLE shift_templates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    days_of_week VARCHAR(50),
    tenant_id VARCHAR(50),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Shift assignments table
CREATE TABLE shift_assignments (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    shift_template_id BIGINT NOT NULL REFERENCES shift_templates(id),
    assignment_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'SCHEDULED',
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_shift_assignments_employee ON shift_assignments(employee_id);
CREATE INDEX idx_shift_assignments_date ON shift_assignments(assignment_date);

-- Attendance events table
CREATE TABLE attendance_events (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    event_type VARCHAR(20) NOT NULL,
    event_timestamp TIMESTAMP NOT NULL,
    device_id VARCHAR(50),
    geo_location VARCHAR(100),
    shift_assignment_id BIGINT REFERENCES shift_assignments(id),
    approved BOOLEAN DEFAULT FALSE,
    correction_requested BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_attendance_employee ON attendance_events(employee_id);
CREATE INDEX idx_attendance_timestamp ON attendance_events(event_timestamp);

-- Leave requests table
CREATE TABLE leave_requests (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    leave_type VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    approver_id BIGINT REFERENCES employees(id),
    accrual_balance DECIMAL(10,2),
    reason TEXT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_leave_requests_employee ON leave_requests(employee_id);
CREATE INDEX idx_leave_requests_status ON leave_requests(status);

-- Certifications table
CREATE TABLE certifications (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    validity_period_days INT,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Employee certifications table
CREATE TABLE employee_certifications (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    certification_id BIGINT NOT NULL REFERENCES certifications(id),
    issue_date DATE NOT NULL,
    expiry_date DATE,
    proof_document_url VARCHAR(255),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_employee_certifications_employee ON employee_certifications(employee_id);
CREATE INDEX idx_employee_certifications_expiry ON employee_certifications(expiry_date);

-- Safety incidents table
CREATE TABLE safety_incidents (
    id BIGSERIAL PRIMARY KEY,
    incident_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    location VARCHAR(100),
    description TEXT,
    status VARCHAR(20) DEFAULT 'OPEN',
    reported_by BIGINT REFERENCES employees(id),
    incident_date TIMESTAMP NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_safety_incidents_status ON safety_incidents(status);
CREATE INDEX idx_safety_incidents_date ON safety_incidents(incident_date);

-- Assets table
CREATE TABLE assets (
    id BIGSERIAL PRIMARY KEY,
    asset_type VARCHAR(50) NOT NULL,
    asset_id VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    condition VARCHAR(20) DEFAULT 'GOOD',
    requires_certification BOOLEAN DEFAULT FALSE,
    certification_id BIGINT REFERENCES certifications(id),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Asset assignments table
CREATE TABLE asset_assignments (
    id BIGSERIAL PRIMARY KEY,
    asset_id BIGINT NOT NULL REFERENCES assets(id),
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    assigned_at TIMESTAMP NOT NULL,
    returned_at TIMESTAMP,
    condition_at_return VARCHAR(20),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_asset_assignments_asset ON asset_assignments(asset_id);
CREATE INDEX idx_asset_assignments_employee ON asset_assignments(employee_id);

-- Audit log table
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    actor VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(50) NOT NULL,
    action VARCHAR(20) NOT NULL,
    before_state TEXT,
    after_state TEXT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(50),
    user_agent TEXT
);

CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_actor ON audit_logs(actor);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);

-- Insert sample data for testing
INSERT INTO employees (badge_id, name, role, department, shift_group, hire_date, status, email, phone)
VALUES 
    ('EMP001', 'John Doe', 'ADMIN', 'Management', 'DAY_SHIFT', '2020-01-15', 'ACTIVE', 'john.doe@company.com', '+1234567890'),
    ('EMP002', 'Jane Smith', 'HR', 'Human Resources', 'DAY_SHIFT', '2021-03-20', 'ACTIVE', 'jane.smith@company.com', '+1234567891'),
    ('EMP003', 'Bob Johnson', 'SUPERVISOR', 'Shipping', 'DAY_SHIFT', '2019-06-10', 'ACTIVE', 'bob.johnson@company.com', '+1234567892'),
    ('EMP004', 'Alice Williams', 'WORKER', 'Shipping', 'DAY_SHIFT', '2022-02-01', 'ACTIVE', 'alice.williams@company.com', '+1234567893');

-- Insert sample certifications
INSERT INTO certifications (name, description, validity_period_days)
VALUES 
    ('Forklift Operator', 'Certification to operate forklifts', 365),
    ('Safety Training', 'Basic warehouse safety training', 730),
    ('Hazmat Handling', 'Hazardous materials handling certification', 365);

COMMIT;