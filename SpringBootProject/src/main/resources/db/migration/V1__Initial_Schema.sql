-- Employee Table
CREATE TABLE employee (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    badge_id VARCHAR(64) UNIQUE NOT NULL,
    role VARCHAR(32) NOT NULL,
    department VARCHAR(128),
    shift_group VARCHAR(64),
    hire_date DATE,
    status VARCHAR(16) NOT NULL,
    deleted BOOLEAN DEFAULT FALSE
);

-- Attendance Table
CREATE TABLE attendance (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    clock_in TIMESTAMP,
    clock_out TIMESTAMP,
    device_id VARCHAR(64),
    geofence VARCHAR(128),
    correction_status VARCHAR(16) DEFAULT 'NONE'
);

-- Shift Template Table
CREATE TABLE shift_template (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    recurrence VARCHAR(64),
    blackout_dates DATE[]
);

-- Shift Assignment Table
CREATE TABLE shift_assignment (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    shift_template_id BIGINT NOT NULL REFERENCES shift_template(id),
    date DATE NOT NULL,
    overtime BOOLEAN DEFAULT FALSE
);

-- Leave Request Table
CREATE TABLE leave_request (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    type VARCHAR(16) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(16) NOT NULL,
    accrual_balance DOUBLE PRECISION
);

-- Certification Table
CREATE TABLE certification (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    type VARCHAR(32) NOT NULL,
    issue_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    proof_document_url VARCHAR(512)
);

-- Safety Incident Table
CREATE TABLE safety_incident (
    id BIGSERIAL PRIMARY KEY,
    severity VARCHAR(32) NOT NULL,
    location VARCHAR(128),
    description TEXT,
    status VARCHAR(16) NOT NULL
);

-- Safety Incident - Involved Employees (Many-to-Many)
CREATE TABLE safety_incident_employee (
    safety_incident_id BIGINT REFERENCES safety_incident(id),
    employee_id BIGINT REFERENCES employee(id),
    PRIMARY KEY (safety_incident_id, employee_id)
);

-- Asset Table
CREATE TABLE asset (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(64) NOT NULL,
    condition VARCHAR(32),
    assigned_to BIGINT REFERENCES employee(id),
    checkout_date DATE,
    return_date DATE
);

-- Performance Review Table
CREATE TABLE performance_review (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id),
    cycle VARCHAR(32) NOT NULL,
    goals TEXT,
    competencies TEXT,
    rating INTEGER,
    comments TEXT,
    status VARCHAR(16) NOT NULL
);

-- Notification Table
CREATE TABLE notification (
    id BIGSERIAL PRIMARY KEY,
    recipient BIGINT REFERENCES employee(id),
    channel VARCHAR(16) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(16),
    quiet_hours_start TIME,
    quiet_hours_end TIME
);

-- Audit Log Table
CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    actor VARCHAR(128) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    entity VARCHAR(64) NOT NULL,
    action VARCHAR(32) NOT NULL,
    before_value TEXT,
    after_value TEXT
);
