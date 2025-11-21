-- Baseline schema for Employee, Role, Attendance, Shift, Leave, Certification, SafetyIncident, Asset, Review, Audit, Notification

CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE employees (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    badge_id VARCHAR(50) UNIQUE NOT NULL,
    role_id INT REFERENCES roles(id),
    department VARCHAR(100),
    shift_group VARCHAR(50),
    hire_date DATE,
    status VARCHAR(20) NOT NULL,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE attendance (
    id SERIAL PRIMARY KEY,
    employee_id INT REFERENCES employees(id),
    clock_in TIMESTAMP,
    clock_out TIMESTAMP,
    device VARCHAR(100),
    geofence_valid BOOLEAN,
    hours_worked DECIMAL(5,2),
    correction_requested BOOLEAN DEFAULT FALSE
);

CREATE TABLE shifts (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50),
    start_time TIME,
    end_time TIME,
    rotation VARCHAR(50),
    overtime_rule VARCHAR(100),
    blackout_date DATE,
    created_by INT REFERENCES employees(id)
);

CREATE TABLE leaves (
    id SERIAL PRIMARY KEY,
    employee_id INT REFERENCES employees(id),
    type VARCHAR(20),
    start_date DATE,
    end_date DATE,
    status VARCHAR(20),
    accrual_balance DECIMAL(5,2)
);

CREATE TABLE certifications (
    id SERIAL PRIMARY KEY,
    employee_id INT REFERENCES employees(id),
    name VARCHAR(100),
    expiration_date DATE,
    document_url VARCHAR(255),
    status VARCHAR(20)
);

CREATE TABLE safety_incidents (
    id SERIAL PRIMARY KEY,
    reported_by INT REFERENCES employees(id),
    severity VARCHAR(20),
    location VARCHAR(100),
    description TEXT,
    status VARCHAR(20),
    investigation_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE assets (
    id SERIAL PRIMARY KEY,
    type VARCHAR(50),
    serial_number VARCHAR(50) UNIQUE,
    assigned_to INT REFERENCES employees(id),
    checkout_time TIMESTAMP,
    return_time TIMESTAMP,
    condition VARCHAR(50),
    overdue BOOLEAN DEFAULT FALSE
);

CREATE TABLE reviews (
    id SERIAL PRIMARY KEY,
    employee_id INT REFERENCES employees(id),
    supervisor_id INT REFERENCES employees(id),
    cycle VARCHAR(20),
    goals TEXT,
    competencies TEXT,
    rating INT,
    comments TEXT,
    acknowledged BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE audit_log (
    id SERIAL PRIMARY KEY,
    actor_id INT REFERENCES employees(id),
    entity VARCHAR(50),
    entity_id INT,
    action VARCHAR(20),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    before_state TEXT,
    after_state TEXT
);

CREATE TABLE notifications (
    id SERIAL PRIMARY KEY,
    employee_id INT REFERENCES employees(id),
    type VARCHAR(20),
    message TEXT,
    delivered BOOLEAN DEFAULT FALSE,
    channel VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);