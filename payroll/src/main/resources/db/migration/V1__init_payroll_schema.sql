CREATE TABLE payroll_exports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    export_date DATETIME NOT NULL,
    format VARCHAR(32) NOT NULL,
    file_path VARCHAR(256) NOT NULL,
    status VARCHAR(32) NOT NULL,
    reconciliation_status VARCHAR(32)
);
CREATE INDEX idx_payroll_export_date ON payroll_exports(export_date);
