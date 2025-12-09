-- V1__Initial_Schema.sql
CREATE TABLE employee (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    badge_id VARCHAR(50) UNIQUE NOT NULL,
    role VARCHAR(30) NOT NULL,
    department VARCHAR(50),
    shift_group VARCHAR(50),
    hire_date DATE,
    status VARCHAR(20),
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50)
);
CREATE TABLE attendance (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    clock_in TIMESTAMP,
    clock_out TIMESTAMP,
    shift_id INTEGER,
    location VARCHAR(100),
    device VARCHAR(100),
    hours_worked NUMERIC(5,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50)
);
CREATE TABLE shift (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    start_time TIME,
    end_time TIME,
    warehouse_id INTEGER,
    recurrence VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50)
);
CREATE TABLE schedule (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    shift_id INTEGER REFERENCES shift(id),
    date DATE,
    status VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50)
);
CREATE TABLE leave (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    type VARCHAR(30),
    start_date DATE,
    end_date DATE,
    status VARCHAR(20),
    approved_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50)
);
CREATE TABLE certification (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    type VARCHAR(50),
    issue_date DATE,
    expiry_date DATE,
    document_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50)
);
CREATE TABLE safety_incident (
    id SERIAL PRIMARY KEY,
    reported_by INTEGER REFERENCES employee(id),
    severity VARCHAR(20),
    location VARCHAR(100),
    description TEXT,
    status VARCHAR(20),
    investigation_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50)
);
CREATE TABLE asset (
    id SERIAL PRIMARY KEY,
    type VARCHAR(50),
    serial_number VARCHAR(50) UNIQUE,
    status VARCHAR(20),
    assigned_to INTEGER REFERENCES employee(id),
    checkout_date TIMESTAMP,
    return_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50)
);
CREATE TABLE performance_review (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    reviewer_id INTEGER REFERENCES employee(id),
    period VARCHAR(20),
    goals TEXT,
    ratings TEXT,
    status VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50)
);
CREATE TABLE audit_log (
    id SERIAL PRIMARY KEY,
    entity_type VARCHAR(50),
    entity_id INTEGER,
    action VARCHAR(20),
    actor VARCHAR(50),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    before_value TEXT,
    after_value TEXT
);
