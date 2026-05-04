-- Baseline schema for Warehouse Employee Management System
-- V1__baseline_schema.sql

-- Employee table
CREATE TABLE employee (
    id BIGSERIAL PRIMARY KEY,
    badge_id VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    role VARCHAR(32) NOT NULL,
    department VARCHAR(64),
    shift_group VARCHAR(32),
    hire_date DATE NOT NULL,
    status VARCHAR(16) NOT NULL,
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64),
    updated_by VARCHAR(64)
);

CREATE INDEX idx_employee_badge_id ON employee(badge_id);
CREATE INDEX idx_employee_department ON employee(department);
CREATE INDEX idx_employee_role ON employee(role);
CREATE INDEX idx_employee_deleted ON employee(deleted);

-- Attendance events table
CREATE TABLE attendance_event (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    event_type VARCHAR(16) NOT NULL, -- CLOCK_IN, CLOCK_OUT
    event_timestamp TIMESTAMP NOT NULL,
    device_id VARCHAR(64),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    shift_id BIGINT,
    hours_worked DECIMAL(5, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_attendance_employee ON attendance_event(employee_id);
CREATE INDEX idx_attendance_timestamp ON attendance_event(event_timestamp);

-- Shift template table
CREATE TABLE shift_template (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    recurrence_rule VARCHAR(128),
    department VARCHAR(64),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Schedule assignment table
CREATE TABLE schedule_assignment (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    shift_template_id BIGINT NOT NULL REFERENCES shift_template(id),
    assignment_date DATE NOT NULL,
    status VARCHAR(16) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64)
);

CREATE INDEX idx_schedule_employee ON schedule_assignment(employee_id);
CREATE INDEX idx_schedule_date ON schedule_assignment(assignment_date);

-- Leave request table
CREATE TABLE leave_request (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    leave_type VARCHAR(32) NOT NULL, -- PTO, SICK, UNPAID
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(16) NOT NULL, -- PENDING, APPROVED, DENIED
    reason TEXT,
    approved_by BIGINT REFERENCES employee(id),
    approved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_leave_employee ON leave_request(employee_id);
CREATE INDEX idx_leave_status ON leave_request(status);

-- Leave balance table
CREATE TABLE leave_balance (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    leave_type VARCHAR(32) NOT NULL,
    balance_hours DECIMAL(6, 2) NOT NULL,
    accrual_rate DECIMAL(5, 2),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(employee_id, leave_type)
);

-- Certification table
CREATE TABLE certification (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    certification_name VARCHAR(128) NOT NULL,
    issue_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    status VARCHAR(16) NOT NULL, -- ACTIVE, EXPIRED, PENDING
    document_url VARCHAR(512),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_cert_employee ON certification(employee_id);
CREATE INDEX idx_cert_expiry ON certification(expiry_date);

-- Safety incident table
CREATE TABLE safety_incident (
    id BIGSERIAL PRIMARY KEY,
    incident_date TIMESTAMP NOT NULL,
    location VARCHAR(128) NOT NULL,
    severity VARCHAR(16) NOT NULL, -- LOW, MEDIUM, HIGH, CRITICAL
    description TEXT NOT NULL,
    status VARCHAR(16) NOT NULL, -- OPEN, INVESTIGATING, RESOLVED
    reported_by BIGINT NOT NULL REFERENCES employee(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_incident_date ON safety_incident(incident_date);
CREATE INDEX idx_incident_status ON safety_incident(status);

-- Asset table
CREATE TABLE asset (
    id BIGSERIAL PRIMARY KEY,
    asset_tag VARCHAR(64) NOT NULL UNIQUE,
    asset_type VARCHAR(64) NOT NULL,
    description VARCHAR(256),
    status VARCHAR(16) NOT NULL, -- AVAILABLE, ASSIGNED, MAINTENANCE
    condition VARCHAR(16) NOT NULL, -- GOOD, FAIR, POOR
    requires_certification VARCHAR(128),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_asset_tag ON asset(asset_tag);
CREATE INDEX idx_asset_status ON asset(status);

-- Asset assignment table
CREATE TABLE asset_assignment (
    id BIGSERIAL PRIMARY KEY,
    asset_id BIGINT NOT NULL REFERENCES asset(id),
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    checkout_date TIMESTAMP NOT NULL,
    expected_return_date TIMESTAMP,
    actual_return_date TIMESTAMP,
    status VARCHAR(16) NOT NULL, -- CHECKED_OUT, RETURNED, OVERDUE
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_asset_assign_employee ON asset_assignment(employee_id);
CREATE INDEX idx_asset_assign_status ON asset_assignment(status);

-- Performance review table
CREATE TABLE performance_review (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    review_period VARCHAR(32) NOT NULL,
    review_date DATE NOT NULL,
    reviewer_id BIGINT NOT NULL REFERENCES employee(id),
    overall_rating DECIMAL(3, 2),
    comments TEXT,
    status VARCHAR(16) NOT NULL, -- DRAFT, SUBMITTED, ACKNOWLEDGED
    acknowledged_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_review_employee ON performance_review(employee_id);
CREATE INDEX idx_review_date ON performance_review(review_date);

-- Notification table
CREATE TABLE notification (
    id BIGSERIAL PRIMARY KEY,
    recipient_id BIGINT NOT NULL REFERENCES employee(id),
    channel VARCHAR(16) NOT NULL, -- EMAIL, SMS, IN_APP
    subject VARCHAR(256) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(16) NOT NULL, -- PENDING, SENT, FAILED
    sent_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notif_recipient ON notification(recipient_id);
CREATE INDEX idx_notif_status ON notification(status);

-- Audit log table
CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(64) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(16) NOT NULL, -- CREATE, UPDATE, DELETE
    actor VARCHAR(64) NOT NULL,
    before_state TEXT,
    after_state TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_entity ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_timestamp ON audit_log(timestamp);
CREATE INDEX idx_audit_actor ON audit_log(actor);

-- User authentication table
CREATE TABLE app_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(256) NOT NULL,
    employee_id BIGINT REFERENCES employee(id),
    role VARCHAR(32) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_username ON app_user(username);

-- Tenant table for multi-tenancy
CREATE TABLE tenant (
    id BIGSERIAL PRIMARY KEY,
    tenant_code VARCHAR(32) NOT NULL UNIQUE,
    tenant_name VARCHAR(128) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert default tenant
INSERT INTO tenant (tenant_code, tenant_name) VALUES ('DEFAULT', 'Default Warehouse');

-- Insert sample admin user (password: admin123)
INSERT INTO app_user (username, password_hash, role) 
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN');