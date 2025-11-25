-- Flyway Migration: Initial Schema for Warehouse EMS

-- Employee Table
CREATE TABLE employee (
    id BIGSERIAL PRIMARY KEY,
    badge_id VARCHAR(50) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    phone VARCHAR(20),
    hire_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    tenant_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_employee_badge_id ON employee(badge_id);
CREATE INDEX idx_employee_tenant_id ON employee(tenant_id);

-- Attendance Event Table
CREATE TABLE attendance_event (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    event_type VARCHAR(50) NOT NULL,
    event_time TIMESTAMP NOT NULL,
    shift_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_attendance_employee_id ON attendance_event(employee_id);

-- Shift Template Table
CREATE TABLE shift_template (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    recurrence_pattern VARCHAR(50),
    tenant_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_shift_template_tenant_id ON shift_template(tenant_id);

-- Leave Request Table
CREATE TABLE leave_request (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    leave_type VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_leave_employee_id ON leave_request(employee_id);

-- Certification Table
CREATE TABLE certification (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    name VARCHAR(100) NOT NULL,
    issued_date DATE NOT NULL,
    expiry_date DATE,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_certification_employee_id ON certification(employee_id);

-- Safety Incident Table
CREATE TABLE safety_incident (
    id BIGSERIAL PRIMARY KEY,
    reported_by BIGINT NOT NULL REFERENCES employee(id),
    incident_type VARCHAR(100) NOT NULL,
    description TEXT,
    incident_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_safety_reported_by ON safety_incident(reported_by);

-- Asset Table
CREATE TABLE asset (
    id BIGSERIAL PRIMARY KEY,
    asset_tag VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    assigned_to BIGINT REFERENCES employee(id),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_asset_tag ON asset(asset_tag);
CREATE INDEX idx_asset_assigned_to ON asset(assigned_to);

-- Performance Review Table
CREATE TABLE performance_review (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    reviewer_id BIGINT REFERENCES employee(id),
    review_date DATE NOT NULL,
    score INTEGER,
    comments TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_review_employee_id ON performance_review(employee_id);

-- Notification Table
CREATE TABLE notification (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT REFERENCES employee(id),
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    sent_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_notification_employee_id ON notification(employee_id);

-- Audit Log Table
CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    action VARCHAR(100) NOT NULL,
    performed_by BIGINT REFERENCES employee(id),
    entity VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    details TEXT
);
CREATE INDEX idx_audit_performed_by ON audit_log(performed_by);

-- Tenant Table
CREATE TABLE tenant (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_tenant_name ON tenant(name);
