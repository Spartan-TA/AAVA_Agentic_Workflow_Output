-- V2__create_attendance_table.sql
CREATE TABLE attendance_events (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    employee_id BIGINT NOT NULL,
    event_type VARCHAR(16) NOT NULL,
    event_time TIMESTAMP NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    location_description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_attendance_employee FOREIGN KEY (employee_id) REFERENCES employees(id)
);

CREATE INDEX idx_attendance_employee ON attendance_events(employee_id, event_time);
