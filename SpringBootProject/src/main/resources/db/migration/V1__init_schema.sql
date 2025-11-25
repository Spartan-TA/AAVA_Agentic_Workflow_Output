-- Flyway migration V1: Initial schema for Warehouse EMS
-- Creates all core tables for employee management, attendance, scheduling, safety, assets, and compliance

-- Employee table: Core employee master data
CREATE TABLE employee (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    badge_id VARCHAR(50) NOT NULL UNIQUE,
    role VARCHAR(30) NOT NULL CHECK (role IN ('ADMIN', 'HR', 'SUPERVISOR', 'WORKER')),
    department VARCHAR(50),
    shift_group VARCHAR(50),
    hire_date DATE,
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE', 'TERMINATED')),
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Certification table: Training and certification tracking
CREATE TABLE certification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    issue_date DATE NOT NULL,
    expiry_date DATE,
    proof_document VARCHAR(255),
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'EXPIRING', 'EXPIRED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE
);

-- Shift table: Shift templates and schedules
CREATE TABLE shift (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    rotation VARCHAR(50),
    overtime_rule VARCHAR(100),
    blackout_date DATE,
    calendar VARCHAR(50),
    warehouse_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Attendance table: Clock-in/out events
CREATE TABLE attendance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    clock_in TIMESTAMP,
    clock_out TIMESTAMP,
    device_info VARCHAR(100),
    geofence_location VARCHAR(100),
    shift_id BIGINT,
    hours_worked DECIMAL(5,2),
    correction_requested BOOLEAN DEFAULT FALSE,
    correction_status VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE,
    FOREIGN KEY (shift_id) REFERENCES shift(id)
);

-- Leave request table: PTO, sick, unpaid leave management
CREATE TABLE leave_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('PTO', 'SICK', 'UNPAID')),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'DENIED')),
    accrual_balance DECIMAL(10,2),
    approved_by BIGINT,
    approved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES employee(id)
);

-- Safety incident table: Incident and near-miss tracking
CREATE TABLE safety_incident (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    severity VARCHAR(20) CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    location VARCHAR(100),
    description TEXT,
    status VARCHAR(20) DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'INVESTIGATING', 'RESOLVED')),
    reported_by BIGINT,
    reported_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    investigation_notes TEXT,
    corrective_actions TEXT,
    resolved_at TIMESTAMP,
    FOREIGN KEY (reported_by) REFERENCES employee(id)
);

-- Asset table: Equipment and PPE tracking
CREATE TABLE asset (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) CHECK (type IN ('SCANNER', 'FORKLIFT', 'PPE', 'OTHER')),
    asset_id VARCHAR(50) UNIQUE,
    condition VARCHAR(50) CHECK (condition IN ('NEW', 'GOOD', 'FAIR', 'POOR')),
    assigned_to BIGINT,
    checked_out_at TIMESTAMP,
    checked_in_at TIMESTAMP,
    expected_return_date DATE,
    overdue BOOLEAN DEFAULT FALSE,
    requires_certification BOOLEAN DEFAULT FALSE,
    certification_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (assigned_to) REFERENCES employee(id)
);

-- Performance review table: Employee reviews and goals
CREATE TABLE performance_review (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    review_cycle VARCHAR(20) CHECK (review_cycle IN ('QUARTERLY', 'ANNUAL')),
    goals TEXT,
    competencies TEXT,
    rating INT CHECK (rating BETWEEN 1 AND 5),
    comments TEXT,
    supervisor_id BIGINT,
    supervisor_ack BOOLEAN DEFAULT FALSE,
    employee_ack BOOLEAN DEFAULT FALSE,
    pdf_export VARCHAR(255),
    immutable BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE,
    FOREIGN KEY (supervisor_id) REFERENCES employee(id)
);

-- Audit log table: Compliance and change tracking
CREATE TABLE audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    entity VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    actor VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    action VARCHAR(20) NOT NULL CHECK (action IN ('CREATE', 'UPDATE', 'DELETE')),
    before_state TEXT,
    after_state TEXT,
    ip_address VARCHAR(50),
    user_agent VARCHAR(255)
);

-- Notification table: In-app, email, SMS notifications
CREATE TABLE notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    channel VARCHAR(20) CHECK (channel IN ('IN_APP', 'EMAIL', 'SMS')),
    subject VARCHAR(255),
    message TEXT,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SENT', 'FAILED')),
    sent_at TIMESTAMP,
    read_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE
);

-- Warehouse table: Multi-warehouse support
CREATE TABLE warehouse (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(255),
    calendar VARCHAR(50),
    timezone VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Payroll export table: Payroll integration tracking
CREATE TABLE payroll_export (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    pay_period_start DATE NOT NULL,
    pay_period_end DATE NOT NULL,
    export_format VARCHAR(50),
    file_path VARCHAR(255),
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED')),
    exported_by BIGINT,
    exported_at TIMESTAMP,
    delivery_method VARCHAR(20) CHECK (delivery_method IN ('SFTP', 'API', 'MANUAL')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (exported_by) REFERENCES employee(id)
);

-- Create indexes for performance optimization
CREATE INDEX idx_employee_badge_id ON employee(badge_id);
CREATE INDEX idx_employee_department ON employee(department);
CREATE INDEX idx_employee_status ON employee(status);
CREATE INDEX idx_certification_employee ON certification(employee_id);
CREATE INDEX idx_certification_expiry ON certification(expiry_date);
CREATE INDEX idx_attendance_employee ON attendance(employee_id);
CREATE INDEX idx_attendance_shift ON attendance(shift_id);
CREATE INDEX idx_leave_employee ON leave_request(employee_id);
CREATE INDEX idx_leave_status ON leave_request(status);
CREATE INDEX idx_incident_status ON safety_incident(status);
CREATE INDEX idx_asset_assigned ON asset(assigned_to);
CREATE INDEX idx_audit_entity ON audit_log(entity, entity_id);
CREATE INDEX idx_audit_timestamp ON audit_log(timestamp);
CREATE INDEX idx_notification_employee ON notification(employee_id);
CREATE INDEX idx_notification_status ON notification(status);