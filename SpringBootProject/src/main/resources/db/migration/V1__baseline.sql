-- Baseline schema for Warehouse Employee Management System
CREATE TABLE employees (
    id SERIAL PRIMARY KEY,
    employee_number VARCHAR(50) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    hire_date DATE NOT NULL,
    active BOOLEAN NOT NULL
);

CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE employee_roles (
    employee_id BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (employee_id, role_id)
);

CREATE TABLE attendance_events (
    id SERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    event_time TIMESTAMP NOT NULL,
    event_type VARCHAR(10) NOT NULL
);

CREATE TABLE shift_templates (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    active BOOLEAN NOT NULL
);

CREATE TABLE leave_requests (
    id SERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    leave_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL
);

CREATE TABLE certifications (
    id SERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    date_issued DATE NOT NULL,
    date_expiry DATE
);

CREATE TABLE safety_incidents (
    id SERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    incident_time TIMESTAMP NOT NULL,
    description VARCHAR(255) NOT NULL,
    severity VARCHAR(50) NOT NULL,
    corrective_action VARCHAR(255)
);

CREATE TABLE assets (
    id SERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    asset_tag VARCHAR(100) NOT NULL,
    asset_type VARCHAR(100) NOT NULL,
    description VARCHAR(255)
);

CREATE TABLE performance_reviews (
    id SERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    review_date DATE NOT NULL,
    reviewer VARCHAR(100) NOT NULL,
    summary VARCHAR(255) NOT NULL,
    goals VARCHAR(255)
);

CREATE TABLE audit_logs (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    details VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP NOT NULL
);

CREATE TABLE notifications (
    id SERIAL PRIMARY KEY,
    message VARCHAR(255) NOT NULL,
    sent_at TIMESTAMP NOT NULL,
    target_role VARCHAR(50) NOT NULL
);
