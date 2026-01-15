CREATE TABLE performance_reviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    review_date DATE NOT NULL,
    reviewer VARCHAR(128) NOT NULL,
    comments TEXT NOT NULL,
    rating VARCHAR(32) NOT NULL
);
CREATE INDEX idx_performance_employee_id ON performance_reviews(employee_id);
