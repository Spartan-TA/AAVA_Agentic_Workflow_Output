-- Flyway migration for attendance_events table
CREATE TABLE attendance_events (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    event_time TIMESTAMP NOT NULL,
    event_type VARCHAR(10) NOT NULL,
    notes VARCHAR(255)
);
CREATE INDEX idx_attendance_employee_id ON attendance_events(employee_id);
CREATE INDEX idx_attendance_event_time ON attendance_events(event_time);
