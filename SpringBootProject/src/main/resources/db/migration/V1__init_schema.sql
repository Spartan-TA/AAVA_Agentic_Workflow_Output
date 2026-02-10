CREATE TABLE employee (
    id SERIAL PRIMARY KEY,
    badge_id VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    role VARCHAR(32) NOT NULL,
    department VARCHAR(64),
    shift_group VARCHAR(64),
    hire_date DATE,
    status VARCHAR(32) NOT NULL,
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE attendance_event (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(id),
    event_type VARCHAR(16) NOT NULL,
    event_time TIMESTAMP NOT NULL,
    device_id VARCHAR(64),
    geofence_location VARCHAR(128),
    approved BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE shift_template (
    id SERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    recurrence VARCHAR(32),
    blackout_dates TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE leave_request (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(id),
    leave_type VARCHAR(32) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    approver_id INTEGER REFERENCES employee(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE certification (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(id),
    cert_type VARCHAR(64) NOT NULL,
    issue_date DATE,
    expiry_date DATE,
    document_url VARCHAR(256),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE safety_incident (
    id SERIAL PRIMARY KEY,
    reported_by INTEGER NOT NULL REFERENCES employee(id),
    description TEXT NOT NULL,
    severity VARCHAR(16),
    location VARCHAR(128),
    status VARCHAR(32) NOT NULL,
    occurred_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE asset (
    id SERIAL PRIMARY KEY,
    asset_tag VARCHAR(64) NOT NULL UNIQUE,
    type VARCHAR(32) NOT NULL,
    assigned_to INTEGER REFERENCES employee(id),
    condition VARCHAR(32),
    checked_out_at TIMESTAMP,
    checked_in_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE performance_review (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(id),
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

CREATE TABLE audit_log (
    id SERIAL PRIMARY KEY,
    entity VARCHAR(64) NOT NULL,
    entity_id INTEGER NOT NULL,
    action VARCHAR(16) NOT NULL,
    actor VARCHAR(64) NOT NULL,
    before_state TEXT,
    after_state TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notification (
    id SERIAL PRIMARY KEY,
    recipient_id INTEGER REFERENCES employee(id),
    channel VARCHAR(16) NOT NULL,
    subject VARCHAR(128),
    message TEXT NOT NULL,
    status VARCHAR(16) NOT NULL,
    sent_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);