-- Baseline schema for Warehouse Employee Management System

CREATE TABLE employee (
    id SERIAL PRIMARY KEY,
    badge_id VARCHAR(32) UNIQUE NOT NULL,
    first_name VARCHAR(64) NOT NULL,
    last_name VARCHAR(64) NOT NULL,
    email VARCHAR(128) NOT NULL,
    phone VARCHAR(32),
    role VARCHAR(32) NOT NULL,
    department VARCHAR(64),
    hire_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE attendance_event (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    timestamp TIMESTAMP NOT NULL,
    type VARCHAR(16) NOT NULL,
    device VARCHAR(64),
    location VARCHAR(128),
    clock_out_time TIMESTAMP,
    total_hours NUMERIC(5,2),
    approval_status VARCHAR(32)
);

CREATE TABLE shift_template (
    id SERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    recurrence_rule VARCHAR(128),
    required_skills VARCHAR(128),
    min_employees INTEGER,
    max_employees INTEGER
);

CREATE TABLE leave_request (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    type VARCHAR(32) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    approver VARCHAR(128),
    reason TEXT
);

CREATE TABLE certification (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    type VARCHAR(64) NOT NULL,
    issue_date DATE NOT NULL,
    expiry_date DATE,
    document_url VARCHAR(256),
    active BOOLEAN DEFAULT TRUE
);

CREATE TABLE safety_incident (
    id SERIAL PRIMARY KEY,
    incident_number VARCHAR(32) UNIQUE NOT NULL,
    incident_date DATE NOT NULL,
    severity VARCHAR(16),
    location VARCHAR(128),
    description TEXT,
    osha_reportable BOOLEAN DEFAULT FALSE
);

CREATE TABLE safety_incident_employee (
    safety_incident_id INTEGER REFERENCES safety_incident(id),
    employee_id INTEGER REFERENCES employee(id),
    PRIMARY KEY (safety_incident_id, employee_id)
);

CREATE TABLE asset (
    id SERIAL PRIMARY KEY,
    asset_tag VARCHAR(32) UNIQUE NOT NULL,
    type VARCHAR(32) NOT NULL,
    condition VARCHAR(32),
    assigned_to INTEGER REFERENCES employee(id),
    assignment_date DATE,
    required_certification VARCHAR(64)
);

CREATE TABLE performance_review (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    reviewer VARCHAR(128),
    cycle VARCHAR(32),
    review_date DATE,
    overall_rating INTEGER,
    comments TEXT,
    status VARCHAR(32)
);

CREATE TABLE warehouse (
    id SERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    location VARCHAR(128),
    timezone VARCHAR(32)
);

CREATE TABLE regional_settings (
    id SERIAL PRIMARY KEY,
    warehouse_id INTEGER REFERENCES warehouse(id),
    language VARCHAR(32),
    date_format VARCHAR(32)
);

CREATE TABLE forecast (
    id SERIAL PRIMARY KEY,
    date DATE NOT NULL,
    demand INTEGER,
    confidence NUMERIC(3,2),
    warehouse_id INTEGER REFERENCES warehouse(id)
);

CREATE TABLE user_profile (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES employee(id),
    contact_info VARCHAR(128),
    photo_url VARCHAR(256)
);

-- Indexes for performance
CREATE INDEX idx_employee_badge_id ON employee(badge_id);
CREATE INDEX idx_attendance_event_employee_id ON attendance_event(employee_id);
CREATE INDEX idx_leave_request_employee_id ON leave_request(employee_id);
CREATE INDEX idx_certification_employee_id ON certification(employee_id);
CREATE INDEX idx_asset_assigned_to ON asset(assigned_to);
CREATE INDEX idx_performance_review_employee_id ON performance_review(employee_id);