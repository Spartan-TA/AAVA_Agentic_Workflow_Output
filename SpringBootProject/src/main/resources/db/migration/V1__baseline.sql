-- Baseline schema for Warehouse EMS
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(32) NOT NULL UNIQUE
);

CREATE TABLE employees (
    id SERIAL PRIMARY KEY,
    badge_id VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    role_id INTEGER REFERENCES roles(id),
    department VARCHAR(64),
    shift_group VARCHAR(64),
    hire_date DATE,
    status VARCHAR(32) NOT NULL,
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE attendance_events (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employees(id),
    event_type VARCHAR(16) NOT NULL, -- CLOCK_IN, CLOCK_OUT
    event_time TIMESTAMP NOT NULL,
    device_info VARCHAR(128),
    location VARCHAR(128),
    approved BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE shift_templates (
    id SERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    recurrence VARCHAR(32),
    blackout_dates TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE shift_assignments (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employees(id),
    shift_template_id INTEGER REFERENCES shift_templates(id),
    assignment_date DATE NOT NULL,
    overtime BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE leave_requests (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employees(id),
    leave_type VARCHAR(32) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE certifications (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employees(id),
    cert_type VARCHAR(64) NOT NULL,
    issue_date DATE,
    expiry_date DATE,
    document_url VARCHAR(256),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE safety_incidents (
    id SERIAL PRIMARY KEY,
    reported_by INTEGER REFERENCES employees(id),
    severity VARCHAR(16),
    location VARCHAR(128),
    description TEXT,
    status VARCHAR(32),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE assets (
    id SERIAL PRIMARY KEY,
    asset_tag VARCHAR(64) NOT NULL UNIQUE,
    type VARCHAR(64) NOT NULL,
    condition VARCHAR(32),
    assigned_to INTEGER REFERENCES employees(id),
    checked_out_at TIMESTAMP,
    checked_in_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE asset_assignments (
    id SERIAL PRIMARY KEY,
    asset_id INTEGER REFERENCES assets(id),
    employee_id INTEGER REFERENCES employees(id),
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    returned_at TIMESTAMP
);

CREATE TABLE performance_reviews (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employees(id),
    review_period VARCHAR(32),
    reviewer_id INTEGER REFERENCES employees(id),
    rating INTEGER,
    comments TEXT,
    acknowledged BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE audit_logs (
    id SERIAL PRIMARY KEY,
    entity VARCHAR(64) NOT NULL,
    entity_id INTEGER,
    action VARCHAR(16) NOT NULL,
    actor_id INTEGER REFERENCES employees(id),
    before_state JSONB,
    after_state JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
