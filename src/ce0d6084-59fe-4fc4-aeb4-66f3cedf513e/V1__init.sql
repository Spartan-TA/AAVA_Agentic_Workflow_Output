CREATE TABLE employees (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    badge_id VARCHAR(50) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL,
    department_id BIGINT,
    shift_group_id BIGINT,
    hire_date DATE,
    status VARCHAR(50),
    deleted BOOLEAN DEFAULT FALSE
);
