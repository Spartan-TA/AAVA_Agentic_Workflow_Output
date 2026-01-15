CREATE TABLE leave_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type VARCHAR(32) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    balance DOUBLE NOT NULL,
    employee_id BIGINT NOT NULL
);
CREATE INDEX idx_leave_employee_id ON leave_requests(employee_id);
