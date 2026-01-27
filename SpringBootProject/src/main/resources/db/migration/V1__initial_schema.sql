-- Flyway migration for Warehouse EMS initial schema
CREATE TABLE employee (
    id BIGSERIAL PRIMARY KEY,
    badge_id VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(100) NOT NULL,
    department VARCHAR(100),
    shift_group VARCHAR(100),
    hire_date DATE,
    status VARCHAR(50) NOT NULL,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE certification (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    validity_months INT NOT NULL
);

CREATE TABLE employee_certification (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    certification_id BIGINT NOT NULL REFERENCES certification(id),
    issue_date DATE,
    expiry_date DATE,
    document_url VARCHAR(512)
);

CREATE TABLE attendance_event (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    event_time TIMESTAMP NOT NULL,
    type VARCHAR(20) NOT NULL,
    device_id VARCHAR(100),
    geofence_id VARCHAR(100),
    correction BOOLEAN DEFAULT FALSE
);

CREATE TABLE shift_template (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    recurring BOOLEAN DEFAULT FALSE
);

CREATE TABLE shift_assignment (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    shift_template_id BIGINT NOT NULL REFERENCES shift_template(id),
    assignment_date DATE NOT NULL
);

CREATE TABLE leave_request (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    type VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL
);

CREATE TABLE safety_incident (
    id BIGSERIAL PRIMARY KEY,
    severity VARCHAR(50) NOT NULL,
    location VARCHAR(255),
    description TEXT,
    status VARCHAR(20) NOT NULL
);

CREATE TABLE safety_incident_employees (
    safety_incident_id BIGINT NOT NULL REFERENCES safety_incident(id),
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    PRIMARY KEY (safety_incident_id, employee_id)
);

CREATE TABLE asset (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(100) NOT NULL,
    serial_number VARCHAR(100) NOT NULL,
    condition VARCHAR(50)
);

CREATE TABLE asset_assignment (
    id BIGSERIAL PRIMARY KEY,
    asset_id BIGINT NOT NULL REFERENCES asset(id),
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    checkout_time TIMESTAMP,
    return_time TIMESTAMP
);

CREATE TABLE performance_review (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    template VARCHAR(255),
    goals TEXT,
    comments TEXT,
    ratings TEXT,
    acknowledged_by_employee BOOLEAN DEFAULT FALSE,
    acknowledged_by_supervisor BOOLEAN DEFAULT FALSE
);

CREATE TABLE notification (
    id BIGSERIAL PRIMARY KEY,
    recipient_id BIGINT NOT NULL REFERENCES employee(id),
    channel VARCHAR(20) NOT NULL,
    message TEXT NOT NULL,
    delivered BOOLEAN DEFAULT FALSE,
    sent_at TIMESTAMP
);

CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    entity VARCHAR(100) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    actor VARCHAR(100) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    before_state TEXT,
    after_state TEXT
);
