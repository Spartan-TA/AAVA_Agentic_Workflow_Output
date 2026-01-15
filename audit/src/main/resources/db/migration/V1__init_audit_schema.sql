CREATE TABLE audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    action VARCHAR(32) NOT NULL,
    entity VARCHAR(128) NOT NULL,
    entity_id BIGINT NOT NULL,
    username VARCHAR(64) NOT NULL,
    timestamp DATETIME NOT NULL,
    details TEXT NOT NULL
);
CREATE INDEX idx_audit_entity ON audit_logs(entity);
CREATE INDEX idx_audit_username ON audit_logs(username);
