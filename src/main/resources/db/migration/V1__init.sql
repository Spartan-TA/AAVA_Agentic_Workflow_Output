-- Initial schema for Warehouse Employee Management System
CREATE TABLE employee (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    badge_id VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL,
    department VARCHAR(100) NOT NULL,
    shift_group VARCHAR(100),
    hire_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE attendance_event (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    type VARCHAR(10) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    device_id VARCHAR(100),
    location VARCHAR(255),
    approved BOOLEAN NOT NULL DEFAULT FALSE,
    correction BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE shift_template (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    recurring BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE shift_assignment (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    shift_template_id BIGINT NOT NULL REFERENCES shift_template(id),
    date DATE NOT NULL
);

CREATE TABLE leave_request (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    type VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    reason TEXT
);

CREATE TABLE certification (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    valid_days INTEGER NOT NULL
);

CREATE TABLE employee_certification (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    certification_id BIGINT NOT NULL REFERENCES certification(id),
    issue_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    document_url VARCHAR(255)
);

CREATE TABLE safety_incident (
    id BIGSERIAL PRIMARY KEY,
    date DATE NOT NULL,
    severity VARCHAR(20) NOT NULL,
    location VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(20) NOT NULL
);

CREATE TABLE safety_incident_employee (
    safety_incident_id BIGINT NOT NULL REFERENCES safety_incident(id),
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    PRIMARY KEY (safety_incident_id, employee_id)
);

CREATE TABLE asset (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(100) NOT NULL,
    serial_number VARCHAR(100) NOT NULL,
    condition VARCHAR(20) NOT NULL
);

CREATE TABLE asset_assignment (
    id BIGSERIAL PRIMARY KEY,
    asset_id BIGINT NOT NULL REFERENCES asset(id),
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    checkout_time TIMESTAMP NOT NULL,
    return_time TIMESTAMP
);

CREATE TABLE performance_review (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    template_id BIGINT NOT NULL,
    review_date DATE NOT NULL,
    comments TEXT,
    acknowledged_by_employee BOOLEAN NOT NULL DEFAULT FALSE,
    acknowledged_by_supervisor BOOLEAN NOT NULL DEFAULT FALSE,
    signed_off BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE review_template (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE goal (
    id BIGSERIAL PRIMARY KEY,
    review_id BIGINT NOT NULL REFERENCES performance_review(id),
    description TEXT NOT NULL
);

CREATE TABLE notification (
    id BIGSERIAL PRIMARY KEY,
    recipient_id BIGINT REFERENCES employee(id),
    channel VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    delivered BOOLEAN NOT NULL DEFAULT FALSE,
    sent_at TIMESTAMP
);

CREATE TABLE announcement (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE user_notification_preference (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    channel VARCHAR(50) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    actor VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT NOW(),
    entity VARCHAR(100) NOT NULL,
    before TEXT,
    after TEXT,
    action VARCHAR(50) NOT NULL
);

CREATE TABLE leave_balance (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    pto_balance NUMERIC(10,2) NOT NULL DEFAULT 0,
    sick_balance NUMERIC(10,2) NOT NULL DEFAULT 0,
    unpaid_balance NUMERIC(10,2) NOT NULL DEFAULT 0
);

CREATE TABLE leave_policy (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(20) NOT NULL,
    accrual_rate NUMERIC(10,2) NOT NULL
);

CREATE TABLE blackout_date (
    id BIGSERIAL PRIMARY KEY,
    date DATE NOT NULL,
    description VARCHAR(255)
);

CREATE TABLE operation_calendar (
    id BIGSERIAL PRIMARY KEY,
    date DATE NOT NULL,
    is_open BOOLEAN NOT NULL DEFAULT TRUE
);
