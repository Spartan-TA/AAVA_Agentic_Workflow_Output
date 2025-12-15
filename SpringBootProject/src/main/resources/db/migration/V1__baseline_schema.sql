-- Baseline schema for Warehouse EMS

CREATE TABLE employees (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    badge_id VARCHAR(50) NOT NULL UNIQUE,
    role VARCHAR(30) NOT NULL,
    department VARCHAR(50) NOT NULL,
    shift_group VARCHAR(50),
    hire_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE attendance_events (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employees(id),
    type VARCHAR(20) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    device_id VARCHAR(50),
    location VARCHAR(100),
    ip_address VARCHAR(45),
    approved BOOLEAN DEFAULT FALSE,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_employee_department ON employees(department);
CREATE INDEX idx_employee_role ON employees(role);
CREATE INDEX idx_employee_status ON employees(status);
CREATE INDEX idx_attendance_employee_id ON attendance_events(employee_id);
CREATE INDEX idx_attendance_timestamp ON attendance_events(timestamp);
