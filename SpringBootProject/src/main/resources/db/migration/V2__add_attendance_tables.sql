-- V2__add_attendance_tables.sql
CREATE TABLE shift (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_recurring BOOLEAN DEFAULT FALSE,
    rotation_group VARCHAR(50),
    blackout_date DATE,
    warehouse_id INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE attendance (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    shift_id INTEGER REFERENCES shift(id),
    clock_in TIMESTAMP,
    clock_out TIMESTAMP,
    device_info VARCHAR(255),
    geofence_location VARCHAR(255),
    status VARCHAR(20) DEFAULT 'PRESENT',
    correction_requested BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_attendance_employee_id ON attendance(employee_id);
CREATE INDEX idx_attendance_shift_id ON attendance(shift_id);
