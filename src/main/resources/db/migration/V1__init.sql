-- Employees
CREATE TABLE employees (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    badge_id VARCHAR(50) UNIQUE NOT NULL,
    role_id INTEGER REFERENCES roles(id),
    department VARCHAR(100),
    shift_group VARCHAR(100),
    hire_date DATE,
    status VARCHAR(50),
    deleted BOOLEAN DEFAULT FALSE
);

-- Roles
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

-- Attendance Events
CREATE TABLE attendance_events (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employees(id),
    type VARCHAR(20) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    device_id VARCHAR(100),
    geo_location VARCHAR(100)
);

-- Shift Templates
CREATE TABLE shift_templates (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    recurring BOOLEAN DEFAULT FALSE
);

-- Shift Assignments
CREATE TABLE shift_assignments (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employees(id),
    shift_template_id INTEGER REFERENCES shift_templates(id),
    date DATE NOT NULL
);

-- Leave Requests
CREATE TABLE leave_requests (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employees(id),
    type VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL
);

-- Certifications
CREATE TABLE certifications (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    issue_date DATE NOT NULL,
    expiry_date DATE,
    employee_id INTEGER REFERENCES employees(id),
    document_url VARCHAR(255)
);

-- Safety Incidents
CREATE TABLE safety_incidents (
    id SERIAL PRIMARY KEY,
    description TEXT NOT NULL,
    location VARCHAR(100),
    severity VARCHAR(20),
    reported_by INTEGER REFERENCES employees(id),
    status VARCHAR(20) NOT NULL
);

-- Assets
CREATE TABLE assets (
    id SERIAL PRIMARY KEY,
    type VARCHAR(100) NOT NULL,
    serial_number VARCHAR(100) NOT NULL,
    condition VARCHAR(50)
);

-- Asset Assignments
CREATE TABLE asset_assignments (
    id SERIAL PRIMARY KEY,
    asset_id INTEGER REFERENCES assets(id),
    employee_id INTEGER REFERENCES employees(id),
    checkout_time TIMESTAMP,
    return_time TIMESTAMP
);

-- Performance Reviews
CREATE TABLE performance_reviews (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employees(id),
    review_date DATE NOT NULL,
    goals TEXT,
    comments TEXT,
    status VARCHAR(20)
);

-- Audit Logs
CREATE TABLE audit_logs (
    id SERIAL PRIMARY KEY,
    entity VARCHAR(100) NOT NULL,
    entity_id INTEGER NOT NULL,
    action VARCHAR(50) NOT NULL,
    actor VARCHAR(100) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    before_state TEXT,
    after_state TEXT
);
