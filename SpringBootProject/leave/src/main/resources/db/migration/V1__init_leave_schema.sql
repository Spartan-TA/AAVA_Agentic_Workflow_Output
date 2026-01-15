-- Flyway migration script for Leave Management module
-- Version: V1
-- Description: Initialize leave_requests table

-- Create leave_requests table
CREATE TABLE IF NOT EXISTS leave_requests (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    leave_type VARCHAR(50) NOT NULL CHECK (leave_type IN ('PTO', 'SICK', 'UNPAID', 'BEREAVEMENT', 'JURY_DUTY')),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason VARCHAR(500) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'DENIED', 'CANCELLED')),
    approver_id BIGINT,
    approved_at TIMESTAMP,
    approver_comments VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT fk_approver FOREIGN KEY (approver_id) REFERENCES employees(id) ON DELETE SET NULL,
    CONSTRAINT check_dates CHECK (end_date >= start_date)
);

-- Create indexes for performance
CREATE INDEX idx_leave_requests_employee_id ON leave_requests(employee_id);
CREATE INDEX idx_leave_requests_status ON leave_requests(status);
CREATE INDEX idx_leave_requests_dates ON leave_requests(start_date, end_date);
CREATE INDEX idx_leave_requests_approver_id ON leave_requests(approver_id);
CREATE INDEX idx_leave_requests_created_at ON leave_requests(created_at DESC);

-- Create leave_balances table for tracking annual allocations
CREATE TABLE IF NOT EXISTS leave_balances (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    leave_type VARCHAR(50) NOT NULL,
    annual_allocation DECIMAL(5,2) NOT NULL DEFAULT 15.0,
    used_days DECIMAL(5,2) NOT NULL DEFAULT 0.0,
    remaining_days DECIMAL(5,2) NOT NULL DEFAULT 15.0,
    year INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_leave_balance_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT unique_employee_leave_year UNIQUE (employee_id, leave_type, year),
    CONSTRAINT check_balance CHECK (remaining_days >= 0)
);

-- Create index for leave balances
CREATE INDEX idx_leave_balances_employee_year ON leave_balances(employee_id, year);

-- Insert default leave balances for existing employees (example)
-- This would typically be done via application logic or a separate data migration
-- INSERT INTO leave_balances (employee_id, leave_type, annual_allocation, year)
-- SELECT id, 'PTO', 15.0, EXTRACT(YEAR FROM CURRENT_DATE)
-- FROM employees
-- WHERE active = true;

-- Add comments for documentation
COMMENT ON TABLE leave_requests IS 'Stores employee leave requests with approval workflow';
COMMENT ON TABLE leave_balances IS 'Tracks annual leave allocations and usage per employee';
COMMENT ON COLUMN leave_requests.leave_type IS 'Type of leave: PTO, SICK, UNPAID, BEREAVEMENT, JURY_DUTY';
COMMENT ON COLUMN leave_requests.status IS 'Request status: PENDING, APPROVED, DENIED, CANCELLED';
COMMENT ON COLUMN leave_balances.remaining_days IS 'Calculated as annual_allocation - used_days';