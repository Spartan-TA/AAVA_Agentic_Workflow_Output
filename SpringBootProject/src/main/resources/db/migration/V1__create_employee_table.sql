CREATE TABLE employees (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    badge_id VARCHAR(20) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL,
    department VARCHAR(50) NOT NULL,
    shift_group VARCHAR(50),
    hire_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    deleted BOOLEAN DEFAULT FALSE
);
