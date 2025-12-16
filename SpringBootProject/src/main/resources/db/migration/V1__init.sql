-- V1__init.sql: Initial schema for Warehouse EMS
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
CREATE TABLE attendance (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    clock_in TIMESTAMP,
    clock_out TIMESTAMP,
    device_info VARCHAR(128),
    geofence_location VARCHAR(128),
    approved BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE shift (
    id SERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    rotation_pattern VARCHAR(64),
    blackout_dates TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE employee_shift (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    shift_id INTEGER REFERENCES shift(id),
    assigned_date DATE,
    conflict BOOLEAN DEFAULT FALSE
);
CREATE TABLE leave (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    type VARCHAR(32),
    start_date DATE,
    end_date DATE,
    status VARCHAR(16) DEFAULT 'PENDING',
    accrual_balance DECIMAL(8,2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE certification (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    name VARCHAR(64),
    expiry_date DATE,
    document_url VARCHAR(256),
    status VARCHAR(16) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE safety_incident (
    id SERIAL PRIMARY KEY,
    reported_by INTEGER REFERENCES employee(id),
    severity VARCHAR(16),
    location VARCHAR(128),
    description TEXT,
    status VARCHAR(16) DEFAULT 'OPEN',
    investigation_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE asset (
    id SERIAL PRIMARY KEY,
    name VARCHAR(64),
    type VARCHAR(32),
    assigned_to INTEGER REFERENCES employee(id),
    checkout_date TIMESTAMP,
    return_date TIMESTAMP,
    condition VARCHAR(32),
    certification_required VARCHAR(64),
    overdue BOOLEAN DEFAULT FALSE
);
CREATE TABLE performance_review (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    cycle VARCHAR(32),
    goals TEXT,
    competencies TEXT,
    rating INTEGER,
    comments TEXT,
    acknowledged BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE audit_log (
    id SERIAL PRIMARY KEY,
    actor VARCHAR(64),
    entity VARCHAR(64),
    entity_id INTEGER,
    action VARCHAR(16),
    before_state TEXT,
    after_state TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
