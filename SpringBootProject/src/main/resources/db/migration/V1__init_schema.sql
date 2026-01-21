-- Employee Table
CREATE TABLE employee (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    badge_id VARCHAR(50) NOT NULL UNIQUE,
    role VARCHAR(30) NOT NULL,
    department VARCHAR(50),
    shift_group VARCHAR(50),
    hire_date DATE,
    status VARCHAR(20) NOT NULL,
    deleted BOOLEAN DEFAULT FALSE
);

-- Attendance Record Table
CREATE TABLE attendance_record (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(id),
    clock_in TIMESTAMP,
    clock_out TIMESTAMP,
    shift_id INTEGER,
    device_info VARCHAR(100),
    status VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Shift Template Table
CREATE TABLE shift_template (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    rotation_pattern VARCHAR(50)
);

-- Shift Assignment Table
CREATE TABLE shift_assignment (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(id),
    shift_template_id INTEGER NOT NULL REFERENCES shift_template(id),
    assignment_date DATE NOT NULL,
    status VARCHAR(20)
);

-- Leave Request Table
CREATE TABLE leave_request (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(id),
    leave_type VARCHAR(30) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    balance_used DECIMAL(5,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Certification Table
CREATE TABLE certification (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(id),
    cert_type VARCHAR(100) NOT NULL,
    issue_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL
);

-- Safety Incident Table
CREATE TABLE safety_incident (
    id SERIAL PRIMARY KEY,
    severity VARCHAR(20) NOT NULL,
    location VARCHAR(100),
    description TEXT,
    status VARCHAR(30) NOT NULL,
    incident_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Asset Table
CREATE TABLE asset (
    id SERIAL PRIMARY KEY,
    asset_type VARCHAR(50) NOT NULL,
    serial_number VARCHAR(100) UNIQUE,
    condition VARCHAR(30),
    status VARCHAR(20) NOT NULL
);

-- Asset Assignment Table
CREATE TABLE asset_assignment (
    id SERIAL PRIMARY KEY,
    asset_id INTEGER NOT NULL REFERENCES asset(id),
    employee_id INTEGER NOT NULL REFERENCES employee(id),
    check_out_date TIMESTAMP NOT NULL,
    check_in_date TIMESTAMP
);

-- Review Cycle Table
CREATE TABLE review_cycle (
    id SERIAL PRIMARY KEY,
    cycle_name VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL
);

-- Performance Review Table
CREATE TABLE performance_review (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(id),
    cycle_id INTEGER NOT NULL REFERENCES review_cycle(id),
    rating INTEGER,
    comments TEXT,
    status VARCHAR(20) NOT NULL
);

-- Notification Table
CREATE TABLE notification (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    channel VARCHAR(20) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Audit Log Table
CREATE TABLE audit_log (
    id SERIAL PRIMARY KEY,
    entity VARCHAR(50) NOT NULL,
    entity_id INTEGER NOT NULL,
    action VARCHAR(30) NOT NULL,
    actor VARCHAR(100) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    before_value TEXT,
    after_value TEXT
);

-- Document Table
CREATE TABLE document (
    id SERIAL PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id INTEGER NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    expiry_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);