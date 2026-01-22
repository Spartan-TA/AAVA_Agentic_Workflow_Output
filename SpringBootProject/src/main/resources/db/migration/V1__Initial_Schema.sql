-- Initial schema for Warehouse EMS
-- This file creates core tables for all modules (simplified for migration)

CREATE TABLE employee (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    badge_id VARCHAR(50) UNIQUE NOT NULL,
    role VARCHAR(30) NOT NULL,
    department VARCHAR(100),
    shift_group VARCHAR(50),
    hire_date DATE,
    status VARCHAR(30),
    active BOOLEAN DEFAULT TRUE,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE attendance_event (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    timestamp TIMESTAMP NOT NULL,
    type VARCHAR(10) NOT NULL,
    device_id VARCHAR(100),
    location VARCHAR(100)
);

CREATE TABLE shift_template (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    start TIME NOT NULL,
    end TIME NOT NULL,
    recurring BOOLEAN DEFAULT FALSE
);

CREATE TABLE shift_assignment (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    template_id BIGINT NOT NULL REFERENCES shift_template(id),
    date DATE NOT NULL
);

CREATE TABLE blackout_date (
    id BIGSERIAL PRIMARY KEY,
    date DATE NOT NULL,
    reason VARCHAR(255)
);

CREATE TABLE leave_request (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    type VARCHAR(30) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL,
    balance NUMERIC(8,2)
);

CREATE TABLE certification (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    type VARCHAR(100) NOT NULL,
    expiry_date DATE,
    document_url VARCHAR(255)
);

CREATE TABLE safety_incident (
    id BIGSERIAL PRIMARY KEY,
    severity VARCHAR(30),
    location VARCHAR(100),
    description TEXT,
    status VARCHAR(30),
    -- involved_employees handled via join table
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE safety_incident_employee (
    incident_id BIGINT REFERENCES safety_incident(id),
    employee_id BIGINT REFERENCES employee(id),
    PRIMARY KEY (incident_id, employee_id)
);

CREATE TABLE asset (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(100),
    serial_number VARCHAR(100),
    condition VARCHAR(30)
);

CREATE TABLE asset_assignment (
    id BIGSERIAL PRIMARY KEY,
    asset_id BIGINT REFERENCES asset(id),
    employee_id BIGINT REFERENCES employee(id),
    checkout_time TIMESTAMP,
    return_time TIMESTAMP
);

CREATE TABLE performance_review (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT REFERENCES employee(id),
    period_start DATE,
    period_end DATE,
    comments TEXT,
    acknowledged BOOLEAN DEFAULT FALSE
);

CREATE TABLE goal (
    id BIGSERIAL PRIMARY KEY,
    review_id BIGINT REFERENCES performance_review(id),
    description TEXT
);

CREATE TABLE competency (
    id BIGSERIAL PRIMARY KEY,
    review_id BIGINT REFERENCES performance_review(id),
    name VARCHAR(100),
    rating INTEGER
);

CREATE TABLE notification (
    id BIGSERIAL PRIMARY KEY,
    channel VARCHAR(30),
    message TEXT,
    sent_at TIMESTAMP,
    delivered BOOLEAN DEFAULT FALSE
);

CREATE TABLE announcement (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(100),
    message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    actor VARCHAR(100),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    entity VARCHAR(100),
    before TEXT,
    after TEXT
);
