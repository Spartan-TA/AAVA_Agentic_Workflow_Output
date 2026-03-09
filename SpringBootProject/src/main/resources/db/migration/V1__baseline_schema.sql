-- Baseline schema for Warehouse Employee Management System
CREATE TABLE role (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE employee (
    id SERIAL PRIMARY KEY,
    badge_id VARCHAR(32) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    department VARCHAR(100),
    shift_group VARCHAR(100),
    hire_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    role_id INTEGER REFERENCES role(id),
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE attendance_event (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    event_type VARCHAR(16) NOT NULL, -- CLOCK_IN, CLOCK_OUT
    event_time TIMESTAMP NOT NULL,
    device_id VARCHAR(64),
    geofence_location VARCHAR(128),
    approved BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE shift_template (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    recurrence_rule VARCHAR(128),
    blackout BOOLEAN DEFAULT FALSE
);

CREATE TABLE leave_request (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    leave_type VARCHAR(32) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approved_by INTEGER REFERENCES employee(id)
);

CREATE TABLE certification (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    cert_type VARCHAR(64) NOT NULL,
    issue_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    document_url VARCHAR(256),
    status VARCHAR(32) NOT NULL
);

CREATE TABLE safety_incident (
    id SERIAL PRIMARY KEY,
    reported_by INTEGER REFERENCES employee(id),
    incident_date TIMESTAMP NOT NULL,
    location VARCHAR(128),
    severity VARCHAR(32),
    description TEXT,
    status VARCHAR(32) NOT NULL,
    corrective_action TEXT
);

CREATE TABLE asset (
    id SERIAL PRIMARY KEY,
    asset_tag VARCHAR(64) NOT NULL UNIQUE,
    type VARCHAR(64) NOT NULL,
    assigned_to INTEGER REFERENCES employee(id),
    condition VARCHAR(32),
    checked_out_at TIMESTAMP,
    checked_in_at TIMESTAMP
);

CREATE TABLE performance_review (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    review_period VARCHAR(32) NOT NULL,
    reviewer_id INTEGER REFERENCES employee(id),
    goals TEXT,
    competencies TEXT,
    rating INTEGER,
    comments TEXT,
    acknowledged_by_employee BOOLEAN DEFAULT FALSE,
    acknowledged_by_supervisor BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notification (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    type VARCHAR(32) NOT NULL,
    message TEXT NOT NULL,
    channel VARCHAR(32),
    sent_at TIMESTAMP,
    status VARCHAR(32)
);

CREATE TABLE audit_log (
    id SERIAL PRIMARY KEY,
    entity VARCHAR(64) NOT NULL,
    entity_id INTEGER NOT NULL,
    action VARCHAR(16) NOT NULL,
    actor_id INTEGER REFERENCES employee(id),
    before_state JSONB,
    after_state JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
