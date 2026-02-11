CREATE TABLE attendance_events (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    event_time TIMESTAMP NOT NULL,
    event_type VARCHAR(20) NOT NULL
);
