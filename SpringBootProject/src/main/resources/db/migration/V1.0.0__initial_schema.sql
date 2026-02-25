-- Audit function for created_at/updated_at
CREATE OR REPLACE FUNCTION set_audit_fields()
RETURNS TRIGGER AS $$
BEGIN
  IF TG_OP = 'INSERT' THEN
    NEW.created_at := NOW();
    NEW.updated_at := NOW();
  ELSIF TG_OP = 'UPDATE' THEN
    NEW.updated_at := NOW();
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Department table
CREATE TABLE department (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    tenant_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    last_modified_by VARCHAR(100),
    version INTEGER DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE
);

-- Position table
CREATE TABLE position (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    department_id UUID REFERENCES department(id),
    tenant_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    last_modified_by VARCHAR(100),
    version INTEGER DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE
);

-- Employee table
CREATE TABLE employee (
    id UUID PRIMARY KEY,
    badge_id VARCHAR(32) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(32),
    hire_date DATE,
    termination_date DATE,
    status VARCHAR(32) NOT NULL,
    department_id UUID REFERENCES department(id),
    position_id UUID REFERENCES position(id),
    supervisor_id UUID REFERENCES employee(id),
    address JSONB,
    emergency_contact JSONB,
    tenant_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    last_modified_by VARCHAR(100),
    version INTEGER DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_employee_department ON employee(department_id);
CREATE INDEX idx_employee_position ON employee(position_id);
CREATE INDEX idx_employee_supervisor ON employee(supervisor_id);
CREATE INDEX idx_employee_tenant ON employee(tenant_id);
CREATE INDEX idx_employee_status ON employee(status);

-- Attendance record table
CREATE TABLE attendance_record (
    id UUID PRIMARY KEY,
    employee_id UUID REFERENCES employee(id),
    clock_in TIMESTAMP,
    clock_out TIMESTAMP,
    status VARCHAR(32) NOT NULL,
    clock_event_metadata JSONB,
    tenant_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    last_modified_by VARCHAR(100),
    version INTEGER DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_attendance_employee ON attendance_record(employee_id);
CREATE INDEX idx_attendance_status ON attendance_record(status);
CREATE INDEX idx_attendance_tenant ON attendance_record(tenant_id);

-- Attendance correction table
CREATE TABLE attendance_correction (
    id UUID PRIMARY KEY,
    attendance_record_id UUID REFERENCES attendance_record(id),
    employee_id UUID REFERENCES employee(id),
    correction_type VARCHAR(32) NOT NULL,
    correction_status VARCHAR(32) NOT NULL,
    requested_at TIMESTAMP NOT NULL,
    approved_at TIMESTAMP,
    approver_id UUID REFERENCES employee(id),
    notes TEXT,
    tenant_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    last_modified_by VARCHAR(100),
    version INTEGER DEFAULT 0,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_correction_employee ON attendance_correction(employee_id);
CREATE INDEX idx_correction_status ON attendance_correction(correction_status);
CREATE INDEX idx_correction_tenant ON attendance_correction(tenant_id);

-- Triggers for audit fields
CREATE TRIGGER trg_department_audit BEFORE INSERT OR UPDATE ON department FOR EACH ROW EXECUTE FUNCTION set_audit_fields();
CREATE TRIGGER trg_position_audit BEFORE INSERT OR UPDATE ON position FOR EACH ROW EXECUTE FUNCTION set_audit_fields();
CREATE TRIGGER trg_employee_audit BEFORE INSERT OR UPDATE ON employee FOR EACH ROW EXECUTE FUNCTION set_audit_fields();
CREATE TRIGGER trg_attendance_audit BEFORE INSERT OR UPDATE ON attendance_record FOR EACH ROW EXECUTE FUNCTION set_audit_fields();
CREATE TRIGGER trg_correction_audit BEFORE INSERT OR UPDATE ON attendance_correction FOR EACH ROW EXECUTE FUNCTION set_audit_fields();
