-- V1: Create base schema for Warehouse EMS
CREATE TABLE department (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE shift_group (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE employee (
    id SERIAL PRIMARY KEY,
    badge_id VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL,
    department_id INTEGER REFERENCES department(id),
    shift_group_id INTEGER REFERENCES shift_group(id),
    hire_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE shift_template (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL
);

CREATE TABLE shift_assignment (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    shift_template_id INTEGER REFERENCES shift_template(id),
    date DATE NOT NULL
);

CREATE TABLE attendance_event (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    clock_in_time TIMESTAMP,
    clock_out_time TIMESTAMP,
    device_id VARCHAR(100),
    location VARCHAR(100)
);

CREATE TABLE leave_request (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,
    reason TEXT
);

CREATE TABLE leave_balance (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    leave_type VARCHAR(50) NOT NULL,
    balance DECIMAL(5,2) NOT NULL
);

CREATE TABLE certification (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT
);

CREATE TABLE employee_certification (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    certification_id INTEGER REFERENCES certification(id),
    issue_date DATE,
    expiry_date DATE
);

CREATE TABLE safety_incident (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    incident_date DATE NOT NULL,
    description TEXT NOT NULL,
    severity VARCHAR(30)
);

CREATE TABLE asset (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    serial_number VARCHAR(100) UNIQUE,
    status VARCHAR(30) NOT NULL
);

CREATE TABLE asset_assignment (
    id SERIAL PRIMARY KEY,
    asset_id INTEGER REFERENCES asset(id),
    employee_id INTEGER REFERENCES employee(id),
    assigned_date DATE,
    returned_date DATE
);

CREATE TABLE performance_review (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    review_template_id INTEGER,
    review_date DATE NOT NULL,
    score DECIMAL(4,2),
    comments TEXT
);

CREATE TABLE review_template (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT
);

CREATE TABLE goal (
    id SERIAL PRIMARY KEY,
    performance_review_id INTEGER REFERENCES performance_review(id),
    description TEXT NOT NULL,
    status VARCHAR(30)
);

CREATE TABLE notification (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    title VARCHAR(100) NOT NULL,
    message TEXT NOT NULL,
    sent_at TIMESTAMP
);

CREATE TABLE announcement (
    id SERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP
);

CREATE TABLE audit_log (
    id SERIAL PRIMARY KEY,
    actor VARCHAR(100) NOT NULL,
    action VARCHAR(100) NOT NULL,
    entity VARCHAR(100) NOT NULL,
    entity_id INTEGER,
    timestamp TIMESTAMP NOT NULL,
    details TEXT
);
