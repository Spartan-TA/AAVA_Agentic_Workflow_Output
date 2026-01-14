-- V7__create_assets.sql
CREATE TABLE assets (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(100) NOT NULL,
    serial_number VARCHAR(100) NOT NULL UNIQUE,
    condition VARCHAR(50) NOT NULL,
    assigned_to BIGINT REFERENCES employees(id)
);

CREATE TABLE asset_assignments (
    id BIGSERIAL PRIMARY KEY,
    asset_id BIGINT NOT NULL REFERENCES assets(id),
    employee_id BIGINT NOT NULL REFERENCES employees(id),
    checkout_date DATE NOT NULL,
    return_date DATE
);