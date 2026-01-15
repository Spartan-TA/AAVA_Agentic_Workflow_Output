CREATE TABLE safety_incidents (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    severity VARCHAR(32) NOT NULL,
    location VARCHAR(128) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(32) NOT NULL
);
CREATE TABLE incident_employees (
    incident_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    PRIMARY KEY (incident_id, employee_id)
);
CREATE INDEX idx_safety_location ON safety_incidents(location);
