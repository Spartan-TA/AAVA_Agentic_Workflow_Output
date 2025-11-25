-- Baseline migration for employee, role, department, shift, certification, asset, attendance, leave, safety_incident, audit_log, etc.
CREATE TABLE employee (
    id SERIAL PRIMARY KEY,
    badge_id VARCHAR(32) UNIQUE NOT NULL,
    name VARCHAR(128) NOT NULL,
    role VARCHAR(32) NOT NULL,
    department VARCHAR(64),
    shift_group VARCHAR(32),
    hire_date DATE,
    status VARCHAR(16) DEFAULT 'ACTIVE'
);

CREATE TABLE role (
    id SERIAL PRIMARY KEY,
    name VARCHAR(32) UNIQUE NOT NULL
);

CREATE TABLE department (
    id SERIAL PRIMARY KEY,
    name VARCHAR(64) UNIQUE NOT NULL
);

CREATE TABLE shift (
    id SERIAL PRIMARY KEY,
    name VARCHAR(32) NOT NULL,
    start_time TIME,
    end_time TIME,
    rotation_pattern VARCHAR(64)
);

CREATE TABLE attendance (
    id SERIAL PRIMARY KEY,
    employee_id INT REFERENCES employee(id),
    clock_in TIMESTAMP,
    clock_out TIMESTAMP,
    geofence_location VARCHAR(128),
    device_info VARCHAR(128),
    approved BOOLEAN DEFAULT FALSE
);

CREATE TABLE leave (
    id SERIAL PRIMARY KEY,
    employee_id INT REFERENCES employee(id),
    type VARCHAR(32),
    start_date DATE,
    end_date DATE,
    status VARCHAR(16),
    accrual_balance DECIMAL(8,2)
);

CREATE TABLE certification (
    id SERIAL PRIMARY KEY,
    employee_id INT REFERENCES employee(id),
    name VARCHAR(64),
    expiry_date DATE,
    document_url VARCHAR(256)
);

CREATE TABLE safety_incident (
    id SERIAL PRIMARY KEY,
    reported_by INT REFERENCES employee(id),
    severity VARCHAR(16),
    location VARCHAR(128),
    description TEXT,
    status VARCHAR(16),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE asset (
    id SERIAL PRIMARY KEY,
    name VARCHAR(64),
    type VARCHAR(32),
    assigned_to INT REFERENCES employee(id),
    condition VARCHAR(32),
    checked_out_at TIMESTAMP,
    checked_in_at TIMESTAMP
);

CREATE TABLE audit_log (
    id SERIAL PRIMARY KEY,
    entity VARCHAR(64),
    entity_id INT,
    action VARCHAR(16),
    actor VARCHAR(64),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    before_state TEXT,
    after_state TEXT
);