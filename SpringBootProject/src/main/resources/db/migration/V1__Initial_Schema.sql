-- V1__Initial_Schema.sql
-- Warehouse Employee Management System Initial Schema

CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE employees (
    id SERIAL PRIMARY KEY,
    employee_code VARCHAR(50) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(20),
    dob DATE,
    hire_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    department VARCHAR(100),
    position VARCHAR(100),
    supervisor_id INTEGER REFERENCES employees(id),
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE employee_roles (
    employee_id INTEGER REFERENCES employees(id) ON DELETE CASCADE,
    role_id INTEGER REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (employee_id, role_id)
);

CREATE TABLE attendance (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employees(id) ON DELETE CASCADE,
    check_in TIMESTAMP NOT NULL,
    check_out TIMESTAMP,
    shift_id INTEGER,
    status VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE shifts (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    description VARCHAR(255)
);

CREATE TABLE employee_shifts (
    employee_id INTEGER REFERENCES employees(id) ON DELETE CASCADE,
    shift_id INTEGER REFERENCES shifts(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    PRIMARY KEY (employee_id, shift_id, date)
);

CREATE TABLE leaves (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employees(id) ON DELETE CASCADE,
    leave_type VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    reason VARCHAR(255),
    approved_by INTEGER REFERENCES employees(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE trainings (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    valid_until DATE
);

CREATE TABLE employee_trainings (
    employee_id INTEGER REFERENCES employees(id) ON DELETE CASCADE,
    training_id INTEGER REFERENCES trainings(id) ON DELETE CASCADE,
    completion_date DATE,
    certification_number VARCHAR(100),
    PRIMARY KEY (employee_id, training_id)
);

CREATE TABLE safety_incidents (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employees(id) ON DELETE SET NULL,
    incident_date DATE NOT NULL,
    description TEXT NOT NULL,
    severity VARCHAR(20),
    reported_by INTEGER REFERENCES employees(id),
    resolved BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE equipment (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    serial_number VARCHAR(100) UNIQUE,
    status VARCHAR(20) NOT NULL,
    last_maintenance DATE,
    assigned_to INTEGER REFERENCES employees(id)
);

CREATE TABLE performance_reviews (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employees(id) ON DELETE CASCADE,
    review_date DATE NOT NULL,
    reviewer_id INTEGER REFERENCES employees(id),
    score INTEGER,
    comments TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE payroll (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employees(id) ON DELETE CASCADE,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    gross_salary NUMERIC(12,2) NOT NULL,
    net_salary NUMERIC(12,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    processed_at TIMESTAMP
);

CREATE TABLE notifications (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employees(id) ON DELETE CASCADE,
    message TEXT NOT NULL,
    type VARCHAR(50),
    read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE audit_logs (
    id SERIAL PRIMARY KEY,
    entity VARCHAR(100) NOT NULL,
    entity_id INTEGER,
    action VARCHAR(50) NOT NULL,
    performed_by INTEGER REFERENCES employees(id),
    performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    details TEXT
);

CREATE TABLE reports (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    generated_by INTEGER REFERENCES employees(id),
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    parameters TEXT,
    file_path VARCHAR(255)
);

-- Add more tables as needed for onboarding, offboarding, localization, AI scheduling, etc.
