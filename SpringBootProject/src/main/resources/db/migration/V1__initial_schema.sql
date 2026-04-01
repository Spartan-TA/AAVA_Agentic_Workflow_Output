-- V1__initial_schema.sql
-- Core tables for Warehouse Employee Management System

CREATE TABLE warehouse (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE employee (
    id SERIAL PRIMARY KEY,
    badge_id VARCHAR(50) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(50),
    role VARCHAR(50) NOT NULL,
    department VARCHAR(100),
    shift_group VARCHAR(100),
    hire_date DATE,
    status VARCHAR(50) NOT NULL,
    warehouse_id INTEGER REFERENCES warehouse(id),
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE attendance_event (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(id),
    event_type VARCHAR(20) NOT NULL,
    event_time TIMESTAMP NOT NULL,
    device_id VARCHAR(100),
    location VARCHAR(255),
    approved BOOLEAN DEFAULT FALSE,
    correction_requested BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE shift_template (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    recurrence_rule VARCHAR(255),
    overtime_rule VARCHAR(255),
    blackout_dates VARCHAR(255),
    warehouse_id INTEGER REFERENCES warehouse(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE shift_schedule (
    id SERIAL PRIMARY KEY,
    shift_template_id INTEGER REFERENCES shift_template(id),
    date DATE NOT NULL,
    warehouse_id INTEGER REFERENCES warehouse(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE shift_assignment (
    id SERIAL PRIMARY KEY,
    shift_schedule_id INTEGER REFERENCES shift_schedule(id),
    employee_id INTEGER REFERENCES employee(id),
    assigned_by VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE leave_request (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    leave_type VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    reason TEXT,
    approved_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE leave_balance (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    leave_type VARCHAR(50) NOT NULL,
    balance DECIMAL(6,2) NOT NULL,
    accrual_policy VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE certification (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    cert_type VARCHAR(100) NOT NULL,
    issue_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    document_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE safety_incident (
    id SERIAL PRIMARY KEY,
    reported_by INTEGER REFERENCES employee(id),
    incident_date TIMESTAMP NOT NULL,
    severity VARCHAR(50) NOT NULL,
    location VARCHAR(255),
    description TEXT,
    status VARCHAR(50) NOT NULL,
    corrective_action TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE asset (
    id SERIAL PRIMARY KEY,
    asset_tag VARCHAR(100) NOT NULL UNIQUE,
    type VARCHAR(100) NOT NULL,
    condition VARCHAR(50),
    status VARCHAR(50) NOT NULL,
    warehouse_id INTEGER REFERENCES warehouse(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE asset_assignment (
    id SERIAL PRIMARY KEY,
    asset_id INTEGER REFERENCES asset(id),
    employee_id INTEGER REFERENCES employee(id),
    assigned_date DATE NOT NULL,
    returned_date DATE,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE performance_review (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    review_period VARCHAR(50) NOT NULL,
    reviewer VARCHAR(255),
    goals TEXT,
    competencies TEXT,
    ratings TEXT,
    comments TEXT,
    status VARCHAR(50) NOT NULL,
    acknowledged_by_employee BOOLEAN DEFAULT FALSE,
    acknowledged_by_supervisor BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE payroll_export_log (
    id SERIAL PRIMARY KEY,
    export_date TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    file_url VARCHAR(255),
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE notification (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    channel VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    delivery_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE announcement (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    visible_from TIMESTAMP NOT NULL,
    visible_to TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE audit_log (
    id SERIAL PRIMARY KEY,
    entity VARCHAR(100) NOT NULL,
    entity_id INTEGER NOT NULL,
    action VARCHAR(50) NOT NULL,
    actor VARCHAR(255) NOT NULL,
    before_state JSONB,
    after_state JSONB,
    timestamp TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE employee_document (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    document_type VARCHAR(100) NOT NULL,
    file_url VARCHAR(255) NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT NOW(),
    uploaded_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE shift_preference (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    preferred_shift VARCHAR(100) NOT NULL,
    effective_from DATE NOT NULL,
    effective_to DATE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);
