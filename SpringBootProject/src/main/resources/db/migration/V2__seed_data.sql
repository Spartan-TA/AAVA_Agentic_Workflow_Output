-- Seed roles
INSERT INTO employee (name, badge_id, role, department, shift_group, hire_date, status)
VALUES
('Admin User', 'BADGE-ADMIN', 'ADMIN', 'HR', 'A', '2020-01-01', 'ACTIVE'),
('HR User', 'BADGE-HR', 'HR', 'HR', 'A', '2020-01-01', 'ACTIVE'),
('Supervisor User', 'BADGE-SUP', 'SUPERVISOR', 'Ops', 'B', '2020-01-01', 'ACTIVE'),
('Worker User', 'BADGE-WORK', 'WORKER', 'Ops', 'C', '2020-01-01', 'ACTIVE');