-- Warehouse Employee Management System - Initial Database Schema
-- Version: 1.0.0
-- Description: Creates all necessary tables for the warehouse employee management system

-- Employees table
CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    badge_id VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    department VARCHAR(50) NOT NULL,
    shift_group VARCHAR(50),
    hire_date DATE,
    status VARCHAR(20) NOT NULL,
    soft_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Attendance table
CREATE TABLE attendance (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    clock_in TIMESTAMP NOT NULL,
    clock_out TIMESTAMP,
    device_id VARCHAR(100),
    location VARCHAR(200),
    status VARCHAR(20) NOT NULL,
    hours_worked DECIMAL(5,2),
    requires_correction BOOLEAN DEFAULT FALSE,
    correction_reason VARCHAR(500),
    approved_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Certifications table
CREATE TABLE certifications (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    type VARCHAR(100) NOT NULL,
    issue_date DATE,
    expiry_date DATE NOT NULL,
    document_url VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    renewal_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Leave requests table
CREATE TABLE leave_requests (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    type VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    reason VARCHAR(500),
    approved_by BIGINT,
    approval_date DATE,
    denial_reason VARCHAR(500),
    days_requested INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Shift templates table
CREATE TABLE shift_templates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    recurrence VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Safety incidents table
CREATE TABLE safety_incidents (
    id BIGSERIAL PRIMARY KEY,
    severity VARCHAR(20) NOT NULL,
    location VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    corrective_actions TEXT,
    incident_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Assets table
CREATE TABLE assets (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(100) NOT NULL,
    condition VARCHAR(50) NOT NULL,
    assigned_employee_id BIGINT REFERENCES employees(id),
    checkout_date TIMESTAMP,
    return_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Performance reviews table
CREATE TABLE performance_reviews (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    cycle VARCHAR(50) NOT NULL,
    ratings TEXT,
    comments TEXT,
    status VARCHAR(20) NOT NULL,
    review_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Audit logs table
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    actor VARCHAR(100) NOT NULL,
    entity VARCHAR(100) NOT NULL,
    entity_id BIGINT,
    action VARCHAR(50) NOT NULL,
    before_value TEXT,
    after_value TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX idx_employees_badge_id ON employees(badge_id);
CREATE INDEX idx_employees_department ON employees(department);
CREATE INDEX idx_employees_role ON employees(role);
CREATE INDEX idx_attendance_employee_id ON attendance(employee_id);
CREATE INDEX idx_attendance_clock_in ON attendance(clock_in);
CREATE INDEX idx_certifications_employee_id ON certifications(employee_id);
CREATE INDEX idx_certifications_expiry_date ON certifications(expiry_date);
CREATE INDEX idx_leave_requests_employee_id ON leave_requests(employee_id);
CREATE INDEX idx_leave_requests_status ON leave_requests(status);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);

-- Insert sample data for testing
INSERT INTO employees (badge_id, name, role, department, shift_group, hire_date, status) VALUES
('EMP001', 'John Doe', 'ADMIN', 'IT', 'DAY', '2020-01-15', 'ACTIVE'),
('EMP002', 'Jane Smith', 'HR', 'Human Resources', 'DAY', '2019-03-20', 'ACTIVE'),
('EMP003', 'Bob Johnson', 'SUPERVISOR', 'Warehouse', 'NIGHT', '2021-06-10', 'ACTIVE'),
('EMP004', 'Alice Williams', 'WORKER', 'Warehouse', 'DAY', '2022-02-01', 'ACTIVE');

COMMIT;