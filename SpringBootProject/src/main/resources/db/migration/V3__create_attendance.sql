CREATE TABLE attendance_events (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    event_type VARCHAR(30) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    device_info VARCHAR(100),
    geofence VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_attendance_events_employee_id ON attendance_events(employee_id);
CREATE INDEX idx_attendance_events_event_type ON attendance_events(event_type);
