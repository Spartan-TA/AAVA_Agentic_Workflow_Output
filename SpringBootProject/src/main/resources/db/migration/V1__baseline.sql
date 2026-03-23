-- Baseline schema for Warehouse EMS
CREATE TABLE role (
    id SERIAL PRIMARY KEY,
    name VARCHAR(32) NOT NULL UNIQUE
);

CREATE TABLE "user" (
    id SERIAL PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(128) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE user_roles (
    user_id INTEGER NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    role_id INTEGER NOT NULL REFERENCES role(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE employee (
    id SERIAL PRIMARY KEY,
    badge_id VARCHAR(32) NOT NULL UNIQUE,
    first_name VARCHAR(64) NOT NULL,
    last_name VARCHAR(64) NOT NULL,
    department VARCHAR(64) NOT NULL,
    role VARCHAR(32) NOT NULL,
    shift_group VARCHAR(32),
    hire_date DATE NOT NULL,
    status VARCHAR(16) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE attendance_record (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(id),
    clock_in TIMESTAMP NOT NULL,
    clock_out TIMESTAMP,
    device_id VARCHAR(64),
    location VARCHAR(128),
    shift_id INTEGER,
    correction_requested BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE shift (
    id SERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    rotation VARCHAR(32),
    blackout BOOLEAN DEFAULT FALSE
);

CREATE TABLE shift_assignment (
    id SERIAL PRIMARY KEY,
    shift_id INTEGER NOT NULL REFERENCES shift(id),
    employee_id INTEGER NOT NULL REFERENCES employee(id),
    assignment_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE leave_request (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(id),
    type VARCHAR(16) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(16) NOT NULL,
    balance_before NUMERIC(5,2),
    balance_after NUMERIC(5,2),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE certification (
    id SERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    description TEXT,
    valid_months INTEGER NOT NULL
);

CREATE TABLE employee_certification (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(id),
    certification_id INTEGER NOT NULL REFERENCES certification(id),
    issue_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    proof_document VARCHAR(256),
    status VARCHAR(16) NOT NULL
);

CREATE TABLE safety_incident (
    id SERIAL PRIMARY KEY,
    incident_number VARCHAR(32) NOT NULL UNIQUE,
    severity VARCHAR(16) NOT NULL,
    location VARCHAR(128) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(16) NOT NULL,
    reported_by INTEGER REFERENCES employee(id),
    reported_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE asset (
    id SERIAL PRIMARY KEY,
    asset_tag VARCHAR(32) NOT NULL UNIQUE,
    type VARCHAR(32) NOT NULL,
    condition VARCHAR(32) NOT NULL,
    assigned_to INTEGER REFERENCES employee(id),
    checked_out_at TIMESTAMP,
    checked_in_at TIMESTAMP
);

CREATE TABLE audit_log (
    id SERIAL PRIMARY KEY,
    entity VARCHAR(64) NOT NULL,
    entity_id INTEGER NOT NULL,
    action VARCHAR(16) NOT NULL,
    actor VARCHAR(64) NOT NULL,
    before_state JSONB,
    after_state JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
