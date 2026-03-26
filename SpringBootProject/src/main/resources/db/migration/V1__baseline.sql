-- Baseline schema for warehouse employee management

CREATE TABLE employee (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    badge_id VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    role VARCHAR(32) NOT NULL,
    department VARCHAR(64),
    shift_group VARCHAR(64),
    hire_date DATE,
    status VARCHAR(16) NOT NULL,
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(32) NOT NULL UNIQUE
);

CREATE TABLE attendance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    clock_in TIMESTAMP,
    clock_out TIMESTAMP,
    device_info VARCHAR(128),
    geofence_ok BOOLEAN,
    shift_id BIGINT,
    approved BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employee(id)
);

CREATE TABLE shift (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    recurrence VARCHAR(32),
    blackout_date DATE,
    department VARCHAR(64),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE leave_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    type VARCHAR(32) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(16) NOT NULL,
    approved_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employee(id)
);

CREATE TABLE certification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    type VARCHAR(64) NOT NULL,
    issue_date DATE,
    expiry_date DATE,
    document_url VARCHAR(256),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employee(id)
);

CREATE TABLE safety_incident (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reported_by BIGINT NOT NULL,
    description VARCHAR(512),
    severity VARCHAR(16),
    location VARCHAR(128),
    status VARCHAR(32),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reported_by) REFERENCES employee(id)
);

CREATE TABLE asset (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    type VARCHAR(32),
    assigned_to BIGINT,
    condition VARCHAR(32),
    checked_out_at TIMESTAMP,
    checked_in_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (assigned_to) REFERENCES employee(id)
);

CREATE TABLE performance_review (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    period VARCHAR(32),
    goals VARCHAR(512),
    competencies VARCHAR(512),
    rating INT,
    comments VARCHAR(512),
    acknowledged BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employee(id),
    FOREIGN KEY (reviewer_id) REFERENCES employee(id)
);

CREATE TABLE audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    actor VARCHAR(64),
    entity VARCHAR(64),
    entity_id BIGINT,
    action VARCHAR(16),
    before_state CLOB,
    after_state CLOB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add more tables as needed for notifications, payroll, etc.