CREATE TABLE attendance_events (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    device_id VARCHAR(100),
    location VARCHAR(255),
    shift_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    latitude DOUBLE,
    longitude DOUBLE
);

CREATE TABLE correction_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    attendance_event_id BIGINT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    request_date TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL
);
