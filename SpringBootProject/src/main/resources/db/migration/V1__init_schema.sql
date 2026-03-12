-- Flyway Baseline Migration: Initial Schema
CREATE TABLE employee (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    badge_id VARCHAR(50) UNIQUE NOT NULL,
    role VARCHAR(50) NOT NULL,
    department VARCHAR(100),
    shift_group VARCHAR(50),
    hire_date DATE,
    status VARCHAR(20) NOT NULL,
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE certification (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    type VARCHAR(100) NOT NULL,
    expiry_date DATE,
    document_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE attendance (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    clock_in TIMESTAMP,
    clock_out TIMESTAMP,
    device VARCHAR(100),
    geofence VARCHAR(100),
    shift_id INTEGER,
    correction_requested BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE shift (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    recurrence VARCHAR(50),
    blackout_date DATE,
    warehouse_id INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE leave (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    type VARCHAR(50) NOT NULL,
    start_date DATE,
    end_date DATE,
    status VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE asset (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50),
    condition VARCHAR(50),
    assigned_to INTEGER REFERENCES employee(id),
    checked_out_at TIMESTAMP,
    checked_in_at TIMESTAMP,
    overdue BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE safety_incident (
    id SERIAL PRIMARY KEY,
    severity VARCHAR(20),
    location VARCHAR(100),
    description TEXT,
    status VARCHAR(20),
    reported_by INTEGER REFERENCES employee(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE audit_log (
    id SERIAL PRIMARY KEY,
    entity VARCHAR(100),
    entity_id INTEGER,
    action VARCHAR(20),
    actor VARCHAR(100),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    before_state JSONB,
    after_state JSONB
);
