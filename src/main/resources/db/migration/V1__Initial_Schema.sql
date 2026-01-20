-- V1__Initial_Schema.sql
-- Initial schema for Warehouse Employee Management System

CREATE TABLE employee (
    id SERIAL PRIMARY KEY,
    badge_id VARCHAR(32) UNIQUE NOT NULL,
    name VARCHAR(128) NOT NULL,
    role VARCHAR(32) NOT NULL,
    department VARCHAR(64),
    shift_group VARCHAR(64),
    hire_date DATE,
    status VARCHAR(16) DEFAULT 'ACTIVE',
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE attendance_event (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(id),
    event_type VARCHAR(16) NOT NULL,
    event_time TIMESTAMP NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    device_id VARCHAR(64),
    approved BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE shift_template (
    id SERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    recurrence_rule VARCHAR(128),
    blackout_dates TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE shift_assignment (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(id),
    shift_template_id INTEGER NOT NULL REFERENCES shift_template(id),
    assignment_date DATE NOT NULL,
    conflict BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE leave_request (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(id),
    leave_type VARCHAR(32) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(16) DEFAULT 'PENDING',
    approved_by INTEGER REFERENCES employee(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE certification (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(id),
    cert_type VARCHAR(64) NOT NULL,
    expiry_date DATE NOT NULL,
    document_url VARCHAR(256),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE safety_incident (
    id SERIAL PRIMARY KEY,
    reported_by INTEGER NOT NULL REFERENCES employee(id),
    severity VARCHAR(16) NOT NULL,
    location VARCHAR(128),
    description TEXT,
    status VARCHAR(16) DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE asset (
    id SERIAL PRIMARY KEY,
    asset_tag VARCHAR(64) UNIQUE NOT NULL,
    type VARCHAR(32) NOT NULL,
    assigned_to INTEGER REFERENCES employee(id),
    condition VARCHAR(32),
    checked_out_at TIMESTAMP,
    checked_in_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE performance_review (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(id),
    review_period VARCHAR(32) NOT NULL,
    reviewer_id INTEGER REFERENCES employee(id),
    rating INTEGER,
    comments TEXT,
    acknowledged BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE payroll_export (
    id SERIAL PRIMARY KEY,
    export_date DATE NOT NULL,
    status VARCHAR(16) DEFAULT 'PENDING',
    file_url VARCHAR(256),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notification (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    channel VARCHAR(16) NOT NULL,
    message TEXT NOT NULL,
    delivered BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE integration_event (
    id SERIAL PRIMARY KEY,
    event_type VARCHAR(32) NOT NULL,
    payload TEXT NOT NULL,
    processed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE audit_log (
    id SERIAL PRIMARY KEY,
    actor_id INTEGER REFERENCES employee(id),
    entity VARCHAR(32) NOT NULL,
    entity_id INTEGER,
    action VARCHAR(16) NOT NULL,
    before_state TEXT,
    after_state TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE report (
    id SERIAL PRIMARY KEY,
    report_type VARCHAR(32) NOT NULL,
    generated_by INTEGER REFERENCES employee(id),
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    file_url VARCHAR(256)
);

CREATE TABLE mobile_event (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    event_type VARCHAR(32) NOT NULL,
    payload TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE lifecycle_task (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    task_type VARCHAR(32) NOT NULL,
    status VARCHAR(16) DEFAULT 'OPEN',
    due_date DATE,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
