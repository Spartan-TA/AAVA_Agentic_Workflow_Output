-- Employee Table
CREATE TABLE employee (
    id SERIAL PRIMARY KEY,
    badge_id VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(32) NOT NULL,
    department VARCHAR(64),
    shift_group VARCHAR(64),
    hire_date DATE NOT NULL,
    status VARCHAR(16) NOT NULL,
    deleted BOOLEAN DEFAULT FALSE,
    warehouse_id INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Attendance Table
CREATE TABLE attendance (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(id),
    clock_in TIMESTAMP,
    clock_out TIMESTAMP,
    shift_id INTEGER REFERENCES shift(id),
    location VARCHAR(128),
    device VARCHAR(64),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Shift Table
CREATE TABLE shift (
    id SERIAL PRIMARY KEY,
    template VARCHAR(64),
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    recurrence VARCHAR(32),
    warehouse_id INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Leave Table
CREATE TABLE leave (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(id),
    type VARCHAR(32) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(16) NOT NULL,
    balance DECIMAL(5,2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Certification Table
CREATE TABLE certification (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(id),
    name VARCHAR(64) NOT NULL,
    expiry_date DATE NOT NULL,
    proof_document VARCHAR(256),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- SafetyIncident Table
CREATE TABLE safety_incident (
    id SERIAL PRIMARY KEY,
    severity VARCHAR(16) NOT NULL,
    location VARCHAR(128),
    description TEXT,
    status VARCHAR(16) NOT NULL,
    reported_by INTEGER REFERENCES employee(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Asset Table
CREATE TABLE asset (
    id SERIAL PRIMARY KEY,
    type VARCHAR(32) NOT NULL,
    serial_number VARCHAR(64) NOT NULL UNIQUE,
    condition VARCHAR(32),
    assigned_to INTEGER REFERENCES employee(id),
    warehouse_id INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- PerformanceReview Table
CREATE TABLE performance_review (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(id),
    cycle VARCHAR(32) NOT NULL,
    ratings JSONB,
    goals JSONB,
    status VARCHAR(16) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- PayrollExport Table
CREATE TABLE payroll_export (
    id SERIAL PRIMARY KEY,
    period VARCHAR(32) NOT NULL,
    records JSONB,
    delivery_status VARCHAR(16) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Notification Table
CREATE TABLE notification (
    id SERIAL PRIMARY KEY,
    channel VARCHAR(16) NOT NULL,
    recipient VARCHAR(128) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(16) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- AuditLog Table
CREATE TABLE audit_log (
    id SERIAL PRIMARY KEY,
    entity VARCHAR(64) NOT NULL,
    action VARCHAR(16) NOT NULL,
    actor VARCHAR(64) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    before_state JSONB,
    after_state JSONB
);

-- Indexes
CREATE INDEX idx_employee_department ON employee(department);
CREATE INDEX idx_attendance_employee ON attendance(employee_id);
CREATE INDEX idx_shift_warehouse ON shift(warehouse_id);
CREATE INDEX idx_asset_warehouse ON asset(warehouse_id);
CREATE INDEX idx_certification_expiry ON certification(expiry_date);