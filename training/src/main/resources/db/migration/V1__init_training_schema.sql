CREATE TABLE certifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type VARCHAR(128) NOT NULL,
    issue_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    employee_id BIGINT NOT NULL
);
CREATE INDEX idx_certification_employee_id ON certifications(employee_id);
