CREATE TABLE employees (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    badge_id VARCHAR(64) NOT NULL UNIQUE,
    role VARCHAR(64) NOT NULL,
    department VARCHAR(64) NOT NULL,
    shift_group VARCHAR(64) NOT NULL,
    hire_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);
