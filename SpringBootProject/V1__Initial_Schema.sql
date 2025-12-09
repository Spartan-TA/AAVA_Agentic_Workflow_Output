-- Flyway Migration: Initial Schema

CREATE TABLE employees (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    active BOOLEAN NOT NULL
);

CREATE TABLE certifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type VARCHAR(255) NOT NULL,
    issue_date DATE NOT NULL,
    expiry_date DATE,
    employee_id BIGINT NOT NULL,
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

CREATE TABLE attendance_events (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    event_date DATE NOT NULL,
    notes VARCHAR(1000),
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

CREATE TABLE shift_templates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    warehouse_id BIGINT NOT NULL,
    active BOOLEAN NOT NULL,
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id)
);

CREATE TABLE shift_assignments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    shift_template_id BIGINT NOT NULL,
    shift_date DATE NOT NULL,
    warehouse_id BIGINT NOT NULL,
    FOREIGN KEY (employee_id) REFERENCES employees(id),
    FOREIGN KEY (shift_template_id) REFERENCES shift_templates(id),
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id)
);

CREATE TABLE leave_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

CREATE TABLE safety_incidents (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    severity VARCHAR(20) NOT NULL,
    workflow_status VARCHAR(20) NOT NULL,
    location VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    incident_datetime DATETIME NOT NULL
);

CREATE TABLE incident_employees (
    incident_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    PRIMARY KEY (incident_id, employee_id),
    FOREIGN KEY (incident_id) REFERENCES safety_incidents(id),
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

CREATE TABLE incident_attachments (
    incident_id BIGINT NOT NULL,
    attachment_url VARCHAR(1000),
    FOREIGN KEY (incident_id) REFERENCES safety_incidents(id)
);

CREATE TABLE assets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type VARCHAR(20) NOT NULL,
    serial_number VARCHAR(255) UNIQUE NOT NULL,
    condition VARCHAR(20) NOT NULL,
    location VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    current_assignee_id BIGINT,
    FOREIGN KEY (current_assignee_id) REFERENCES employees(id)
);

CREATE TABLE performance_reviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    review_period VARCHAR(255) NOT NULL,
    comments VARCHAR(2000),
    goals VARCHAR(2000),
    status VARCHAR(20) NOT NULL,
    reviewer_id BIGINT,
    review_date DATE,
    FOREIGN KEY (employee_id) REFERENCES employees(id),
    FOREIGN KEY (reviewer_id) REFERENCES employees(id)
);

CREATE TABLE performance_ratings (
    review_id BIGINT NOT NULL,
    criteria VARCHAR(255) NOT NULL,
    rating INT,
    PRIMARY KEY (review_id, criteria),
    FOREIGN KEY (review_id) REFERENCES performance_reviews(id)
);

CREATE TABLE audit_entries (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    actor VARCHAR(255) NOT NULL,
    timestamp DATETIME NOT NULL,
    entity_type VARCHAR(255) NOT NULL,
    entity_id VARCHAR(255) NOT NULL,
    action VARCHAR(20) NOT NULL,
    before_value TEXT,
    after_value TEXT
);

CREATE TABLE notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    recipient_id BIGINT NOT NULL,
    channel VARCHAR(20) NOT NULL,
    type VARCHAR(255) NOT NULL,
    message VARCHAR(2000) NOT NULL,
    status VARCHAR(20) NOT NULL,
    sent_timestamp DATETIME,
    FOREIGN KEY (recipient_id) REFERENCES employees(id)
);

CREATE TABLE warehouses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) UNIQUE NOT NULL,
    location VARCHAR(255) NOT NULL,
    timezone VARCHAR(255) NOT NULL,
    calendar VARCHAR(2000)
);

CREATE TABLE warehouse_policies (
    warehouse_id BIGINT NOT NULL,
    policy VARCHAR(1000),
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id)
);

-- Indexes
CREATE INDEX idx_employee_active ON employees(active);
CREATE INDEX idx_attendance_event_date ON attendance_events(event_date);
CREATE INDEX idx_safety_incident_severity ON safety_incidents(severity);
CREATE INDEX idx_asset_status ON assets(status);
