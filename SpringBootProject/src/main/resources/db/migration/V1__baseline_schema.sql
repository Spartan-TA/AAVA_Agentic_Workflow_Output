-- Flyway Baseline Migration V1
-- Creates core tables for Warehouse Employee Management System
-- Author: Warehouse EMS Team
-- Version: 1.0.0

-- Enable UUID extension for PostgreSQL
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create tenants table for multi-tenant support
CREATE TABLE tenants (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    locale VARCHAR(10) NOT NULL DEFAULT 'en_US',
    timezone VARCHAR(50) NOT NULL DEFAULT 'America/New_York',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create employees table
CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT REFERENCES tenants(id),
    badge_id VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'HR', 'SUPERVISOR', 'WORKER')),
    department VARCHAR(50) NOT NULL,
    shift_group VARCHAR(50),
    hire_date DATE,
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE', 'ON_LEAVE', 'TERMINATED')),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50)
);

-- Create indexes for employees table
CREATE INDEX idx_employee_badge ON employees(badge_id) WHERE deleted = FALSE;
CREATE INDEX idx_employee_department ON employees(department) WHERE deleted = FALSE;
CREATE INDEX idx_employee_status ON employees(status) WHERE deleted = FALSE;
CREATE INDEX idx_employee_tenant ON employees(tenant_id) WHERE deleted = FALSE;
CREATE INDEX idx_employee_role ON employees(role) WHERE deleted = FALSE;

-- Create audit_logs table for compliance
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT REFERENCES tenants(id),
    actor VARCHAR(100) NOT NULL,
    entity VARCHAR(100) NOT NULL,
    entity_id BIGINT,
    action VARCHAR(50) NOT NULL CHECK (action IN ('CREATE', 'UPDATE', 'DELETE', 'READ')),
    before_state TEXT,
    after_state TEXT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent TEXT
);

-- Create index for audit logs
CREATE INDEX idx_audit_timestamp ON audit_logs(timestamp DESC);
CREATE INDEX idx_audit_entity ON audit_logs(entity, entity_id);
CREATE INDEX idx_audit_actor ON audit_logs(actor);

-- Create attendance_events table
CREATE TABLE attendance_events (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT REFERENCES tenants(id),
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    event_type VARCHAR(20) NOT NULL CHECK (event_type IN ('CLOCK_IN', 'CLOCK_OUT')),
    timestamp TIMESTAMP NOT NULL,
    device_id VARCHAR(100),
    location VARCHAR(200),
    shift_id BIGINT,
    correction_status VARCHAR(20) CHECK (correction_status IN ('NONE', 'PENDING', 'APPROVED', 'REJECTED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for attendance_events
CREATE INDEX idx_attendance_employee ON attendance_events(employee_id, timestamp DESC);
CREATE INDEX idx_attendance_timestamp ON attendance_events(timestamp DESC);
CREATE INDEX idx_attendance_tenant ON attendance_events(tenant_id);

-- Create shift_templates table
CREATE TABLE shift_templates (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT REFERENCES tenants(id),
    name VARCHAR(100) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    overtime_eligible BOOLEAN NOT NULL DEFAULT FALSE,
    rotation_pattern VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create shift_assignments table
CREATE TABLE shift_assignments (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT REFERENCES tenants(id),
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    shift_template_id BIGINT NOT NULL REFERENCES shift_templates(id),
    assignment_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(employee_id, assignment_date)
);

-- Create indexes for shift_assignments
CREATE INDEX idx_shift_assignment_employee ON shift_assignments(employee_id, assignment_date DESC);
CREATE INDEX idx_shift_assignment_date ON shift_assignments(assignment_date);

-- Create leave_requests table
CREATE TABLE leave_requests (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT REFERENCES tenants(id),
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    leave_type VARCHAR(20) NOT NULL CHECK (leave_type IN ('PTO', 'SICK', 'UNPAID')),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'APPROVED', 'DENIED')),
    reason TEXT,
    approver_id BIGINT REFERENCES employees(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for leave_requests
CREATE INDEX idx_leave_employee ON leave_requests(employee_id, start_date DESC);
CREATE INDEX idx_leave_status ON leave_requests(status);

-- Create certifications table
CREATE TABLE certifications (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT REFERENCES tenants(id),
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    certification_type VARCHAR(100) NOT NULL,
    issue_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    valid BOOLEAN NOT NULL DEFAULT TRUE,
    document_path VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for certifications
CREATE INDEX idx_cert_employee ON certifications(employee_id);
CREATE INDEX idx_cert_expiry ON certifications(expiry_date) WHERE valid = TRUE;

-- Create safety_incidents table
CREATE TABLE safety_incidents (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT REFERENCES tenants(id),
    severity VARCHAR(20) NOT NULL,
    location VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('OPEN', 'INVESTIGATING', 'RESOLVED')),
    incident_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create incident_employees junction table
CREATE TABLE incident_employees (
    incident_id BIGINT NOT NULL REFERENCES safety_incidents(id),
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    PRIMARY KEY (incident_id, employee_id)
);

-- Create indexes for safety_incidents
CREATE INDEX idx_incident_date ON safety_incidents(incident_date DESC);
CREATE INDEX idx_incident_status ON safety_incidents(status);

-- Create assets table
CREATE TABLE assets (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT REFERENCES tenants(id),
    asset_type VARCHAR(100) NOT NULL,
    serial_number VARCHAR(100) NOT NULL UNIQUE,
    condition VARCHAR(20) NOT NULL CHECK (condition IN ('EXCELLENT', 'GOOD', 'FAIR', 'POOR', 'OUT_OF_SERVICE')),
    required_certification VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create asset_assignments table
CREATE TABLE asset_assignments (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT REFERENCES tenants(id),
    asset_id BIGINT NOT NULL REFERENCES assets(id),
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    checkout_time TIMESTAMP NOT NULL,
    return_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for asset_assignments
CREATE INDEX idx_asset_assignment_employee ON asset_assignments(employee_id);
CREATE INDEX idx_asset_assignment_asset ON asset_assignments(asset_id);
CREATE INDEX idx_asset_assignment_active ON asset_assignments(asset_id) WHERE return_time IS NULL;

-- Create notifications table
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT REFERENCES tenants(id),
    user_id BIGINT NOT NULL REFERENCES employees(id),
    channel VARCHAR(20) NOT NULL CHECK (channel IN ('IN_APP', 'EMAIL', 'SMS')),
    template_key VARCHAR(100) NOT NULL,
    locale VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'SENT', 'FAILED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for notifications
CREATE INDEX idx_notification_user ON notifications(user_id, created_at DESC);
CREATE INDEX idx_notification_status ON notifications(status);

-- Insert default tenant
INSERT INTO tenants (name, locale, timezone) VALUES ('Default Warehouse', 'en_US', 'America/New_York');

-- Create function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create triggers for updated_at
CREATE TRIGGER update_employees_updated_at BEFORE UPDATE ON employees
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_tenants_updated_at BEFORE UPDATE ON tenants
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_shift_templates_updated_at BEFORE UPDATE ON shift_templates
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_leave_requests_updated_at BEFORE UPDATE ON leave_requests
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_certifications_updated_at BEFORE UPDATE ON certifications
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_safety_incidents_updated_at BEFORE UPDATE ON safety_incidents
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_assets_updated_at BEFORE UPDATE ON assets
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Grant permissions (adjust as needed for your environment)
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO warehouse;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO warehouse;

-- Add comments for documentation
COMMENT ON TABLE employees IS 'Core employee master data table';
COMMENT ON TABLE audit_logs IS 'Immutable audit trail for compliance';
COMMENT ON TABLE attendance_events IS 'Time and attendance clock in/out events';
COMMENT ON TABLE shift_templates IS 'Reusable shift templates';
COMMENT ON TABLE shift_assignments IS 'Employee shift assignments';
COMMENT ON TABLE leave_requests IS 'Employee leave and absence requests';
COMMENT ON TABLE certifications IS 'Employee training and certifications';
COMMENT ON TABLE safety_incidents IS 'Safety incident reports';
COMMENT ON TABLE assets IS 'Equipment and asset registry';
COMMENT ON TABLE asset_assignments IS 'Asset checkout/return tracking';
COMMENT ON TABLE notifications IS 'Notification delivery tracking';