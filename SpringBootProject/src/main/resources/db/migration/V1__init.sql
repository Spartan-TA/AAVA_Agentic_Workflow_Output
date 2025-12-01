-- =============================
-- Flyway Migration: V1__init.sql
-- Warehouse EMS Initial Schema for 17 Epics
-- =============================

-- Employee Table
CREATE TABLE employee (
    id SERIAL PRIMARY KEY,
    employee_number VARCHAR(32) NOT NULL UNIQUE,
    first_name VARCHAR(64) NOT NULL,
    last_name VARCHAR(64) NOT NULL,
    email VARCHAR(128) NOT NULL UNIQUE,
    phone VARCHAR(32),
    hire_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    department VARCHAR(64),
    position VARCHAR(64),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Attendance Event Table
CREATE TABLE attendance_event (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(id) ON DELETE CASCADE,
    event_type VARCHAR(32) NOT NULL, -- e.g., CLOCK_IN, CLOCK_OUT, BREAK
    event_time TIMESTAMP NOT NULL,
    location VARCHAR(128),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_attendance_event_employee_id ON attendance_event(employee_id);

-- Shift Template Table
CREATE TABLE shift_template (
    id SERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    break_minutes INTEGER DEFAULT 0,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Shift Assignment Table
CREATE TABLE shift_assignment (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(id) ON DELETE CASCADE,
    shift_template_id INTEGER NOT NULL REFERENCES shift_template(id),
    shift_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_shift_assignment_employee_id ON shift_assignment(employee_id);
CREATE INDEX idx_shift_assignment_shift_template_id ON shift_assignment(shift_template_id);

-- Leave Request Table
CREATE TABLE leave_request (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(id) ON DELETE CASCADE,
    leave_type VARCHAR(32) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_leave_request_employee_id ON leave_request(employee_id);

-- Certification Table
CREATE TABLE certification (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(id) ON DELETE CASCADE,
    name VARCHAR(128) NOT NULL,
    issued_date DATE NOT NULL,
    expiry_date DATE,
    issuer VARCHAR(128),
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_certification_employee_id ON certification(employee_id);

-- Safety Incident Table
CREATE TABLE safety_incident (
    id SERIAL PRIMARY KEY,
    reported_by INTEGER NOT NULL REFERENCES employee(id),
    incident_date TIMESTAMP NOT NULL,
    location VARCHAR(128),
    description TEXT NOT NULL,
    severity VARCHAR(32),
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_safety_incident_reported_by ON safety_incident(reported_by);

-- Asset Table
CREATE TABLE asset (
    id SERIAL PRIMARY KEY,
    asset_tag VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    type VARCHAR(64),
    status VARCHAR(32) NOT NULL,
    assigned_to INTEGER REFERENCES employee(id),
    purchase_date DATE,
    last_maintenance DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_asset_assigned_to ON asset(assigned_to);

-- Performance Review Table
CREATE TABLE performance_review (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(id) ON DELETE CASCADE,
    reviewer_id INTEGER REFERENCES employee(id),
    review_date DATE NOT NULL,
    score INTEGER NOT NULL,
    comments TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_performance_review_employee_id ON performance_review(employee_id);

-- Notification Table
CREATE TABLE notification (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    type VARCHAR(32) NOT NULL, -- EMAIL, SMS, PUSH
    subject VARCHAR(128),
    message TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    sent_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_notification_employee_id ON notification(employee_id);

-- Audit Log Table
CREATE TABLE audit_log (
    id SERIAL PRIMARY KEY,
    action VARCHAR(64) NOT NULL,
    entity VARCHAR(64) NOT NULL,
    entity_id INTEGER,
    performed_by INTEGER REFERENCES employee(id),
    performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    details TEXT
);
CREATE INDEX idx_audit_log_performed_by ON audit_log(performed_by);

-- Additional tables for other epics (examples):

-- Payroll Export Table
CREATE TABLE payroll_export (
    id SERIAL PRIMARY KEY,
    export_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    file_name VARCHAR(128),
    sftp_path VARCHAR(256),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Message Queue Table (for async notifications)
CREATE TABLE message_queue (
    id SERIAL PRIMARY KEY,
    payload TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    retry_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Task Table (for workflow management)
CREATE TABLE task (
    id SERIAL PRIMARY KEY,
    title VARCHAR(128) NOT NULL,
    description TEXT,
    assigned_to INTEGER REFERENCES employee(id),
    due_date DATE,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_task_assigned_to ON task(assigned_to);

-- Document Table (for document management)
CREATE TABLE document (
    id SERIAL PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    type VARCHAR(64),
    url VARCHAR(256) NOT NULL,
    uploaded_by INTEGER REFERENCES employee(id),
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_document_uploaded_by ON document(uploaded_by);

-- Equipment Maintenance Table
CREATE TABLE equipment_maintenance (
    id SERIAL PRIMARY KEY,
    asset_id INTEGER NOT NULL REFERENCES asset(id) ON DELETE CASCADE,
    maintenance_date DATE NOT NULL,
    description TEXT,
    performed_by INTEGER REFERENCES employee(id),
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_equipment_maintenance_asset_id ON equipment_maintenance(asset_id);

-- End of V1__init.sql