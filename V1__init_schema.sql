-- Employees
CREATE TABLE employees (
    id SERIAL PRIMARY KEY,
    badge_id VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(100) NOT NULL,
    department VARCHAR(100),
    shift_group VARCHAR(100),
    hire_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Attendance Events
CREATE TABLE attendance_events (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employees(id),
    timestamp TIMESTAMP NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    device_id VARCHAR(100),
    geo_location VARCHAR(255),
    correction_requested BOOLEAN DEFAULT FALSE
);

-- Shift Templates
CREATE TABLE shift_templates (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    days VARCHAR(50),
    recurring BOOLEAN DEFAULT FALSE
);

-- Shift Assignments
CREATE TABLE shift_assignments (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employees(id),
    template_id INTEGER REFERENCES shift_templates(id),
    date DATE NOT NULL
);

-- Leave Requests
CREATE TABLE leave_requests (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employees(id),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    leave_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    approver VARCHAR(255)
);

-- Certifications
CREATE TABLE certifications (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employees(id),
    type VARCHAR(100) NOT NULL,
    issue_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    document_url VARCHAR(255)
);

-- Safety Incidents
CREATE TABLE safety_incidents (
    id SERIAL PRIMARY KEY,
    description TEXT NOT NULL,
    severity VARCHAR(50) NOT NULL,
    location VARCHAR(255),
    status VARCHAR(50) NOT NULL
);

-- Assets
CREATE TABLE assets (
    id SERIAL PRIMARY KEY,
    asset_tag VARCHAR(100) NOT NULL,
    type VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    assigned_to INTEGER REFERENCES employees(id),
    checkout_date TIMESTAMP,
    checkin_date TIMESTAMP
);

-- Performance Reviews
CREATE TABLE performance_reviews (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employees(id),
    review_date DATE NOT NULL,
    template TEXT,
    goals TEXT,
    rating INTEGER,
    comments TEXT,
    acknowledged_employee BOOLEAN DEFAULT FALSE,
    acknowledged_supervisor BOOLEAN DEFAULT FALSE
);

-- Notifications
CREATE TABLE notifications (
    id SERIAL PRIMARY KEY,
    recipient_id INTEGER REFERENCES employees(id),
    channel VARCHAR(50) NOT NULL,
    template TEXT,
    content TEXT,
    sent_at TIMESTAMP,
    delivered BOOLEAN DEFAULT FALSE
);

-- Audit Logs
CREATE TABLE audit_logs (
    id SERIAL PRIMARY KEY,
    entity VARCHAR(100) NOT NULL,
    entity_id INTEGER NOT NULL,
    actor VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    action VARCHAR(100) NOT NULL,
    before_value TEXT,
    after_value TEXT
);

-- Payroll Exports
CREATE TABLE payroll_exports (
    id SERIAL PRIMARY KEY,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    delivered_at TIMESTAMP
);
