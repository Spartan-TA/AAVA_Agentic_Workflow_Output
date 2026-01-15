CREATE TABLE notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    channel VARCHAR(32) NOT NULL,
    template VARCHAR(128) NOT NULL,
    recipient VARCHAR(128) NOT NULL,
    content TEXT NOT NULL,
    sent_at DATETIME NOT NULL,
    status VARCHAR(32) NOT NULL
);
CREATE INDEX idx_notification_recipient ON notifications(recipient);
