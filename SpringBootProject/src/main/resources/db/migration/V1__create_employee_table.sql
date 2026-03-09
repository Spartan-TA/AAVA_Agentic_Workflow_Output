CREATE TABLE employees (
    id SERIAL PRIMARY KEY,
    badge_id VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    role VARCHAR(64) NOT NULL,
    department VARCHAR(64) NOT NULL,
    shift_group VARCHAR(64) NOT NULL,
    hire_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL
);
