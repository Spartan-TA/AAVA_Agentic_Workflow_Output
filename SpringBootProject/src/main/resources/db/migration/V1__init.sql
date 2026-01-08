-- V1__init.sql: Initial schema for Warehouse Employee Management System

CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(32) NOT NULL UNIQUE
);

CREATE TABLE employees (
    id SERIAL PRIMARY KEY,
    badge_id VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    role_id INTEGER REFERENCES roles(id),
    department VARCHAR(64),
    shift_group VARCHAR(64),
    hire_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE employee_roles (
    employee_id INTEGER REFERENCES employees(id),
    role_id INTEGER REFERENCES roles(id),
    PRIMARY KEY (employee_id, role_id)
);

CREATE TABLE shifts (
    id SERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    recurrence VARCHAR(32),
    blackout BOOLEAN DEFAULT FALSE
);

CREATE TABLE schedules (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employees(id),
    shift_id INTEGER REFERENCES shifts(id),
    date DATE NOT NULL,
    assigned_by INTEGER REFERENCES employees(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE attendance (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employees(id),
    clock_in TIMESTAMP,
    clock_out TIMESTAMP,
    device_info VARCHAR(128),
    geofence_location VARCHAR(128),
    status VARCHAR(32),
    correction_requested BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE leaves (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employees(id),
    type VARCHAR(32) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    approved_by INTEGER REFERENCES employees(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE certifications (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employees(id),
    name VARCHAR(64) NOT NULL,
    issue_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    document_url VARCHAR(256),
    status VARCHAR(32) NOT NULL
);

CREATE TABLE safety_incidents (
    id SERIAL PRIMARY KEY,
    reported_by INTEGER REFERENCES employees(id),
    severity VARCHAR(32),
    location VARCHAR(128),
    description TEXT,
    status VARCHAR(32),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE equipment (
    id SERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    type VARCHAR(32),
    condition VARCHAR(32),
    assigned_to INTEGER REFERENCES employees(id),
    checked_out_at TIMESTAMP,
    checked_in_at TIMESTAMP
);

CREATE TABLE performance_reviews (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employees(id),
    reviewer_id INTEGER REFERENCES employees(id),
    review_period VARCHAR(32),
    goals TEXT,
    competencies TEXT,
    ratings TEXT,
    comments TEXT,
    status VARCHAR(32),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE payroll_exports (
    id SERIAL PRIMARY KEY,
    export_date DATE NOT NULL,
    status VARCHAR(32),
    file_url VARCHAR(256),
    created_by INTEGER REFERENCES employees(id)
);

CREATE TABLE notifications (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employees(id),
    type VARCHAR(32),
    message TEXT,
    channel VARCHAR(32),
    delivered BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE audit_trail (
    id SERIAL PRIMARY KEY,
    entity VARCHAR(64),
    entity_id INTEGER,
    action VARCHAR(32),
    actor_id INTEGER REFERENCES employees(id),
    before_state JSONB,
    after_state JSONB,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Additional tables for reporting, integration, onboarding, localization, etc. can be added as needed.
