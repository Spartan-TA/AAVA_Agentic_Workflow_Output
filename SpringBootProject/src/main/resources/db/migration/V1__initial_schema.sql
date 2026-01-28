-- Employee Table
CREATE TABLE employee (
    id SERIAL PRIMARY KEY,
    badge_id VARCHAR(32) NOT NULL UNIQUE,
    first_name VARCHAR(64) NOT NULL,
    last_name VARCHAR(64) NOT NULL,
    email VARCHAR(128) NOT NULL UNIQUE,
    role VARCHAR(32) NOT NULL,
    department VARCHAR(64) NOT NULL,
    shift_group VARCHAR(32),
    hire_date DATE NOT NULL,
    status VARCHAR(16) NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE
);

-- AttendanceEvent Table
CREATE TABLE attendance_event (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(id),
    event_type VARCHAR(16) NOT NULL,
    event_time TIMESTAMP NOT NULL,
    geofence VARCHAR(128),
    device VARCHAR(64),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE
);

-- ShiftTemplate Table
CREATE TABLE shift_template (
    id SERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    recurrence_pattern VARCHAR(32),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE
);

-- ShiftAssignment Table
CREATE TABLE shift_assignment (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(id),
    shift_template_id INTEGER NOT NULL REFERENCES shift_template(id),
    assignment_date DATE NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE
);

-- LeaveRequest Table
CREATE TABLE leave_request (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(id),
    leave_type VARCHAR(16) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(16) NOT NULL,
    accrual_balance DECIMAL(8,2) DEFAULT 0,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE
);

-- Certification Table
CREATE TABLE certification (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(id),
    cert_type VARCHAR(64) NOT NULL,
    expiry_date DATE NOT NULL,
    status VARCHAR(16) NOT NULL,
    proof_document VARCHAR(256),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE
);

-- SafetyIncident Table
CREATE TABLE safety_incident (
    id SERIAL PRIMARY KEY,
    reported_by INTEGER NOT NULL REFERENCES employee(id),
    severity VARCHAR(16) NOT NULL,
    location VARCHAR(128) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(16) NOT NULL,
    involved_employees VARCHAR(256),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE
);

-- Asset Table
CREATE TABLE asset (
    id SERIAL PRIMARY KEY,
    asset_tag VARCHAR(32) NOT NULL UNIQUE,
    type VARCHAR(32) NOT NULL,
    condition VARCHAR(16) NOT NULL,
    assigned_to INTEGER REFERENCES employee(id),
    checked_out_date TIMESTAMP,
    checked_in_date TIMESTAMP,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE
);

-- Performance ReviewCycle Table
CREATE TABLE review_cycle (
    id SERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(16) NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE
);

-- PayrollExport Table
CREATE TABLE payroll_export (
    id SERIAL PRIMARY KEY,
    export_date TIMESTAMP NOT NULL,
    status VARCHAR(16) NOT NULL,
    file_path VARCHAR(256),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE
);

-- Notification Table
CREATE TABLE notification (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    channel VARCHAR(16) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(16) NOT NULL,
    sent_date TIMESTAMP,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE
);

-- AuditLog Table
CREATE TABLE audit_log (
    id SERIAL PRIMARY KEY,
    actor_id INTEGER REFERENCES employee(id),
    entity VARCHAR(64) NOT NULL,
    entity_id INTEGER NOT NULL,
    action VARCHAR(16) NOT NULL,
    before_state TEXT,
    after_state TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version INTEGER DEFAULT 0
);

-- Indexes for performance
CREATE INDEX idx_employee_badge_id ON employee(badge_id);
CREATE INDEX idx_attendance_event_employee_id ON attendance_event(employee_id);
CREATE INDEX idx_shift_assignment_employee_id ON shift_assignment(employee_id);
CREATE INDEX idx_leave_request_employee_id ON leave_request(employee_id);
CREATE INDEX idx_certification_employee_id ON certification(employee_id);
CREATE INDEX idx_safety_incident_reported_by ON safety_incident(reported_by);
CREATE INDEX idx_asset_asset_tag ON asset(asset_tag);
CREATE INDEX idx_review_cycle_status ON review_cycle(status);
CREATE INDEX idx_payroll_export_status ON payroll_export(status);
CREATE INDEX idx_notification_employee_id ON notification(employee_id);
CREATE INDEX idx_audit_log_actor_id ON audit_log(actor_id);