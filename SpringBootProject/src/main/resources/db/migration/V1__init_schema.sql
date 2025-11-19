-- Flyway Migration V1: Initial Schema for Warehouse Employee Management System
-- Author: System Generated
-- Date: 2025-01-19
-- Description: Creates core tables for employee management, attendance, scheduling, leave, certifications, safety, assets, performance, and audit

-- =====================================================
-- EMPLOYEE MASTER DATA TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS employee (
    id BIGSERIAL PRIMARY KEY,
    badge_id VARCHAR(32) UNIQUE NOT NULL,
    first_name VARCHAR(64) NOT NULL,
    last_name VARCHAR(64) NOT NULL,
    email VARCHAR(128) NOT NULL,
    phone VARCHAR(32),
    role VARCHAR(32) NOT NULL CHECK (role IN ('ADMIN', 'HR', 'SUPERVISOR', 'WORKER')),
    department VARCHAR(64),
    shift_group VARCHAR(64),
    hire_date DATE,
    status VARCHAR(32) NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE', 'TERMINATED', 'ON_LEAVE')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT uk_employee_email UNIQUE (email)
);

CREATE INDEX idx_employee_badge_id ON employee(badge_id);
CREATE INDEX idx_employee_status ON employee(status);
CREATE INDEX idx_employee_department ON employee(department);

COMMENT ON TABLE employee IS 'Master employee data table';
COMMENT ON COLUMN employee.badge_id IS 'Unique employee badge identifier';
COMMENT ON COLUMN employee.role IS 'Employee role: ADMIN, HR, SUPERVISOR, WORKER';
COMMENT ON COLUMN employee.status IS 'Employee status: ACTIVE, INACTIVE, TERMINATED, ON_LEAVE';
COMMENT ON COLUMN employee.deleted_at IS 'Soft delete timestamp';

-- =====================================================
-- ATTENDANCE RECORDS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS attendance_record (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id) ON DELETE CASCADE,
    clock_in_time TIMESTAMP,
    clock_out_time TIMESTAMP,
    shift_id BIGINT,
    location VARCHAR(128),
    device_info VARCHAR(128),
    hours_worked NUMERIC(5,2),
    status VARCHAR(32) CHECK (status IN ('CLOCKED_IN', 'CLOCKED_OUT', 'CORRECTION_PENDING', 'APPROVED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_attendance_employee_id ON attendance_record(employee_id);
CREATE INDEX idx_attendance_clock_in_time ON attendance_record(clock_in_time);
CREATE INDEX idx_attendance_status ON attendance_record(status);

COMMENT ON TABLE attendance_record IS 'Employee clock in/out records';
COMMENT ON COLUMN attendance_record.hours_worked IS 'Calculated hours worked for the shift';
COMMENT ON COLUMN attendance_record.location IS 'Geofence location data';

-- =====================================================
-- SHIFT TEMPLATES TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS shift_template (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    days_of_week VARCHAR(32),
    is_recurring BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_shift_template_name ON shift_template(name);

COMMENT ON TABLE shift_template IS 'Reusable shift templates';
COMMENT ON COLUMN shift_template.days_of_week IS 'Comma-separated days: MON,TUE,WED,THU,FRI,SAT,SUN';

-- =====================================================
-- SHIFT ASSIGNMENTS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS shift_assignment (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id) ON DELETE CASCADE,
    shift_template_id BIGINT NOT NULL REFERENCES shift_template(id) ON DELETE CASCADE,
    assignment_date DATE NOT NULL,
    status VARCHAR(32) CHECK (status IN ('SCHEDULED', 'COMPLETED', 'CANCELLED', 'NO_SHOW')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_shift_assignment UNIQUE (employee_id, assignment_date)
);

CREATE INDEX idx_shift_assignment_employee_id ON shift_assignment(employee_id);
CREATE INDEX idx_shift_assignment_date ON shift_assignment(assignment_date);
CREATE INDEX idx_shift_assignment_status ON shift_assignment(status);

COMMENT ON TABLE shift_assignment IS 'Employee shift assignments';

-- =====================================================
-- LEAVE REQUESTS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS leave_request (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id) ON DELETE CASCADE,
    leave_type VARCHAR(32) NOT NULL CHECK (leave_type IN ('PTO', 'SICK', 'UNPAID', 'BEREAVEMENT', 'JURY_DUTY')),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason TEXT,
    status VARCHAR(32) NOT NULL CHECK (status IN ('PENDING', 'APPROVED', 'DENIED', 'CANCELLED')),
    approver_id BIGINT REFERENCES employee(id),
    approved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_leave_request_employee_id ON leave_request(employee_id);
CREATE INDEX idx_leave_request_status ON leave_request(status);
CREATE INDEX idx_leave_request_dates ON leave_request(start_date, end_date);

COMMENT ON TABLE leave_request IS 'Employee leave requests';

-- =====================================================
-- LEAVE BALANCES TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS leave_balance (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id) ON DELETE CASCADE,
    leave_type VARCHAR(32) NOT NULL CHECK (leave_type IN ('PTO', 'SICK', 'UNPAID', 'BEREAVEMENT', 'JURY_DUTY')),
    balance NUMERIC(5,2) NOT NULL DEFAULT 0,
    accrual_rate NUMERIC(5,2),
    year INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_leave_balance UNIQUE (employee_id, leave_type, year)
);

CREATE INDEX idx_leave_balance_employee_id ON leave_balance(employee_id);

COMMENT ON TABLE leave_balance IS 'Employee leave balances by type and year';

-- =====================================================
-- CERTIFICATIONS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS certification (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id) ON DELETE CASCADE,
    cert_name VARCHAR(128) NOT NULL,
    issue_date DATE NOT NULL,
    expiry_date DATE,
    status VARCHAR(32) CHECK (status IN ('ACTIVE', 'EXPIRED', 'PENDING_RENEWAL')),
    document_url VARCHAR(512),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_certification_employee_id ON certification(employee_id);
CREATE INDEX idx_certification_expiry_date ON certification(expiry_date);
CREATE INDEX idx_certification_status ON certification(status);

COMMENT ON TABLE certification IS 'Employee training and certifications';

-- =====================================================
-- SAFETY INCIDENTS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS safety_incident (
    id BIGSERIAL PRIMARY KEY,
    incident_date TIMESTAMP NOT NULL,
    location VARCHAR(128),
    severity VARCHAR(32) CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    description TEXT NOT NULL,
    involved_employee_ids TEXT,
    reporter_id BIGINT NOT NULL REFERENCES employee(id),
    status VARCHAR(32) CHECK (status IN ('OPEN', 'INVESTIGATING', 'RESOLVED', 'CLOSED')),
    investigation_notes TEXT,
    corrective_actions TEXT,
    osha_recordable BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_safety_incident_date ON safety_incident(incident_date);
CREATE INDEX idx_safety_incident_severity ON safety_incident(severity);
CREATE INDEX idx_safety_incident_status ON safety_incident(status);

COMMENT ON TABLE safety_incident IS 'Safety incident reports';
COMMENT ON COLUMN safety_incident.osha_recordable IS 'OSHA 300 recordable incident';

-- =====================================================
-- ASSETS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS asset (
    id BIGSERIAL PRIMARY KEY,
    asset_type VARCHAR(64) NOT NULL,
    serial_number VARCHAR(64) UNIQUE NOT NULL,
    condition VARCHAR(32) CHECK (condition IN ('NEW', 'GOOD', 'FAIR', 'POOR', 'OUT_OF_SERVICE')),
    certification_required VARCHAR(128),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_asset_type ON asset(asset_type);
CREATE INDEX idx_asset_serial_number ON asset(serial_number);

COMMENT ON TABLE asset IS 'Equipment and asset inventory';

-- =====================================================
-- ASSET ASSIGNMENTS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS asset_assignment (
    id BIGSERIAL PRIMARY KEY,
    asset_id BIGINT NOT NULL REFERENCES asset(id) ON DELETE CASCADE,
    employee_id BIGINT NOT NULL REFERENCES employee(id) ON DELETE CASCADE,
    checkout_date TIMESTAMP NOT NULL,
    return_date TIMESTAMP,
    status VARCHAR(32) CHECK (status IN ('CHECKED_OUT', 'RETURNED', 'OVERDUE', 'LOST')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_asset_assignment_asset_id ON asset_assignment(asset_id);
CREATE INDEX idx_asset_assignment_employee_id ON asset_assignment(employee_id);
CREATE INDEX idx_asset_assignment_status ON asset_assignment(status);

COMMENT ON TABLE asset_assignment IS 'Asset checkout and return tracking';

-- =====================================================
-- PERFORMANCE REVIEWS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS performance_review (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employee(id) ON DELETE CASCADE,
    reviewer_id BIGINT NOT NULL REFERENCES employee(id),
    review_period VARCHAR(32) NOT NULL,
    goals TEXT,
    ratings JSONB,
    comments TEXT,
    status VARCHAR(32) CHECK (status IN ('DRAFT', 'SUBMITTED', 'ACKNOWLEDGED', 'COMPLETED')),
    acknowledged_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_performance_review_employee_id ON performance_review(employee_id);
CREATE INDEX idx_performance_review_status ON performance_review(status);

COMMENT ON TABLE performance_review IS 'Employee performance reviews';

-- =====================================================
-- AUDIT LOG TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(64) NOT NULL,
    entity_id BIGINT NOT NULL,
    action VARCHAR(32) NOT NULL CHECK (action IN ('CREATE', 'UPDATE', 'DELETE', 'READ')),
    actor_id BIGINT REFERENCES employee(id),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    before_state JSONB,
    after_state JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT
);

CREATE INDEX idx_audit_log_entity ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_log_actor_id ON audit_log(actor_id);
CREATE INDEX idx_audit_log_timestamp ON audit_log(timestamp);

COMMENT ON TABLE audit_log IS 'Immutable audit trail for compliance';

-- =====================================================
-- SEED DATA (Optional)
-- =====================================================
-- Insert default admin user
INSERT INTO employee (badge_id, first_name, last_name, email, role, department, status, created_at)
VALUES ('ADMIN001', 'System', 'Administrator', 'admin@warehouse-employee-mgmt.com', 'ADMIN', 'IT', 'ACTIVE', CURRENT_TIMESTAMP)
ON CONFLICT (badge_id) DO NOTHING;

-- =====================================================
-- END OF MIGRATION
-- =====================================================