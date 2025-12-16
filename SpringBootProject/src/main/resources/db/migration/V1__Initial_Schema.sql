CREATE TABLE employees (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(50),
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    employee_id BIGINT,
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE attendance_events (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    event_time TIMESTAMP NOT NULL,
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

CREATE TABLE shift_templates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE shift_template_days (
    shift_template_id BIGINT NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,
    FOREIGN KEY (shift_template_id) REFERENCES shift_templates(id)
);

CREATE TABLE shift_template_blackout_dates (
    shift_template_id BIGINT NOT NULL,
    blackout_date TIMESTAMP NOT NULL,
    FOREIGN KEY (shift_template_id) REFERENCES shift_templates(id)
);

CREATE TABLE shift_assignments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    shift_template_id BIGINT NOT NULL,
    assignment_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    FOREIGN KEY (employee_id) REFERENCES employees(id),
    FOREIGN KEY (shift_template_id) REFERENCES shift_templates(id)
);

CREATE TABLE leave_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    leave_type VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    reason VARCHAR(255),
    approved_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

CREATE TABLE leave_balances (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    leave_type VARCHAR(50) NOT NULL,
    balance DOUBLE NOT NULL,
    accrual_rate DOUBLE NOT NULL,
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

CREATE TABLE employee_certifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    certification_name VARCHAR(255) NOT NULL,
    issue_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    document_url VARCHAR(255),
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

CREATE TABLE safety_incidents (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    incident_date DATE NOT NULL,
    location VARCHAR(255) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    investigation_notes TEXT
);

CREATE TABLE incident_employees (
    incident_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    FOREIGN KEY (incident_id) REFERENCES safety_incidents(id),
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

CREATE TABLE asset_assignments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    asset_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    assigned_date DATE NOT NULL,
    return_date DATE,
    condition VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL
);

CREATE TABLE performance_reviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    review_cycle VARCHAR(50) NOT NULL,
    review_date DATE NOT NULL,
    rating DOUBLE NOT NULL,
    goals TEXT,
    competencies TEXT,
    comments TEXT,
    reviewer_id BIGINT,
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

CREATE TABLE audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    user_id BIGINT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    before_value TEXT,
    after_value TEXT
);

CREATE INDEX idx_employee_id ON employees(id);
CREATE INDEX idx_user_id ON users(id);
CREATE INDEX idx_role_id ON roles(id);
CREATE INDEX idx_attendance_employee ON attendance_events(employee_id);
CREATE INDEX idx_shift_assignment_employee ON shift_assignments(employee_id);
CREATE INDEX idx_leave_employee ON leave_requests(employee_id);
CREATE INDEX idx_cert_employee ON employee_certifications(employee_id);
CREATE INDEX idx_safety_incident_date ON safety_incidents(incident_date);
CREATE INDEX idx_asset_assignment_employee ON asset_assignments(employee_id);
CREATE INDEX idx_performance_employee ON performance_reviews(employee_id);
CREATE INDEX idx_audit_entity ON audit_logs(entity_type);