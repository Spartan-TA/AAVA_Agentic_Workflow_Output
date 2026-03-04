-- Flyway Baseline Migration for Warehouse EMS
CREATE TABLE employees (
    id SERIAL PRIMARY KEY,
    badgeId VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL,
    department VARCHAR(50) NOT NULL,
    hireDate DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    createdAt TIMESTAMP NOT NULL,
    updatedAt TIMESTAMP NOT NULL,
    createdBy VARCHAR(50) NOT NULL,
    updatedBy VARCHAR(50) NOT NULL
);
-- Add other tables as needed for initial baseline
