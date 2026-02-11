-- Add indexes for performance improvements
CREATE INDEX idx_employee_badge_id ON employee(badge_id);
CREATE INDEX idx_attendance_employee_id ON attendance(employee_id);
CREATE INDEX idx_shift_assignment_employee_id ON shift_assignment(employee_id);
CREATE INDEX idx_leave_request_employee_id ON leave_request(employee_id);
