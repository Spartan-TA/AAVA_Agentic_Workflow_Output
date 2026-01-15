CREATE TABLE assets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type VARCHAR(64) NOT NULL,
    serial_number VARCHAR(64) NOT NULL,
    condition VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL
);
CREATE TABLE asset_assignments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    asset_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    assigned_date DATE NOT NULL,
    returned_date DATE
);
CREATE INDEX idx_asset_serial_number ON assets(serial_number);
CREATE INDEX idx_asset_assignment_employee_id ON asset_assignments(employee_id);
