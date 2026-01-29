CREATE TABLE employees (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    badge_id VARCHAR(64) UNIQUE NOT NULL,
    role VARCHAR(32) NOT NULL,
    department VARCHAR(128),
    shift_group VARCHAR(128),
    hire_date DATE,
    status VARCHAR(32) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
