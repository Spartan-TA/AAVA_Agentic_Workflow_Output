-- Employee Table
CREATE TABLE employee (
    id SERIAL PRIMARY KEY,
    badge_id VARCHAR(32) UNIQUE NOT NULL,
    first_name VARCHAR(64) NOT NULL,
    last_name VARCHAR(64) NOT NULL,
    email VARCHAR(128) UNIQUE NOT NULL,
    phone VARCHAR(32),
    department VARCHAR(64),
    role VARCHAR(32) NOT NULL,
    hire_date DATE NOT NULL,
    status VARCHAR(16) NOT NULL,
    tenant_id VARCHAR(32) NOT NULL,
    supervisor_id INTEGER REFERENCES employee(id)
);

-- AttendanceRecord Table
CREATE TABLE attendance_record (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    shift_id INTEGER REFERENCES shift(id),
    clock_in TIMESTAMP,
    clock_out TIMESTAMP,
    location VARCHAR(128),
    device VARCHAR(64),
    status VARCHAR(16)
);

-- Shift Table
CREATE TABLE shift (
    id SERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    department VARCHAR(64),
    is_template BOOLEAN DEFAULT FALSE,
    recurrence_pattern VARCHAR(32)
);

-- LeaveRequest Table
CREATE TABLE leave_request (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    leave_type VARCHAR(32) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(16) NOT NULL,
    approved_by INTEGER REFERENCES employee(id)
);

-- Certification Table
CREATE TABLE certification (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    cert_name VARCHAR(64) NOT NULL,
    issue_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    status VARCHAR(16) NOT NULL,
    document_url VARCHAR(256)
);

-- SafetyIncident Table
CREATE TABLE safety_incident (
    id SERIAL PRIMARY KEY,
    reported_by INTEGER REFERENCES employee(id),
    incident_date DATE NOT NULL,
    location VARCHAR(128),
    severity VARCHAR(16),
    description TEXT,
    status VARCHAR(16),
    osha_recordable BOOLEAN DEFAULT FALSE
);

-- Asset Table
CREATE TABLE asset (
    id SERIAL PRIMARY KEY,
    asset_tag VARCHAR(32) UNIQUE NOT NULL,
    name VARCHAR(64) NOT NULL,
    type VARCHAR(32),
    condition VARCHAR(16),
    assigned_to INTEGER REFERENCES employee(id),
    certification_required VARCHAR(64)
);

-- PerformanceReview Table
CREATE TABLE performance_review (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    reviewer_id INTEGER REFERENCES employee(id),
    period VARCHAR(32),
    status VARCHAR(16),
    rating INTEGER,
    comments TEXT
);

-- AuditLog Table
CREATE TABLE audit_log (
    id SERIAL PRIMARY KEY,
    entity_type VARCHAR(32) NOT NULL,
    entity_id INTEGER NOT NULL,
    action VARCHAR(16) NOT NULL,
    actor VARCHAR(64) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    before_value TEXT,
    after_value TEXT
);

-- Indexes and Constraints
CREATE INDEX idx_employee_department ON employee(department);
CREATE INDEX idx_attendance_employee ON attendance_record(employee_id);
CREATE INDEX idx_shift_department ON shift(department);
CREATE INDEX idx_leave_employee ON leave_request(employee_id);
CREATE INDEX idx_certification_employee ON certification(employee_id);
CREATE INDEX idx_safety_reported_by ON safety_incident(reported_by);
CREATE INDEX idx_asset_assigned_to ON asset(assigned_to);
CREATE INDEX idx_performance_employee ON performance_review(employee_id);
CREATE INDEX idx_audit_entity ON audit_log(entity_type, entity_id);