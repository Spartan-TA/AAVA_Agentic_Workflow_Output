-- Flyway migration: Initial schema
CREATE TABLE department (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE role (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE employee (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    phone VARCHAR(20),
    hire_date DATE NOT NULL,
    department_id INTEGER REFERENCES department(id),
    role_id INTEGER REFERENCES role(id),
    active BOOLEAN DEFAULT TRUE
);

CREATE TABLE attendance (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    date DATE NOT NULL,
    check_in TIMESTAMP,
    check_out TIMESTAMP
);

CREATE TABLE shift (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL
);

CREATE TABLE leave (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL
);

CREATE TABLE certification (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    name VARCHAR(100) NOT NULL,
    issue_date DATE,
    expiry_date DATE
);

CREATE TABLE safety_incident (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    description TEXT NOT NULL,
    incident_date DATE NOT NULL,
    resolved BOOLEAN DEFAULT FALSE
);

CREATE TABLE asset (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    assigned_to INTEGER REFERENCES employee(id),
    assigned_date DATE,
    status VARCHAR(50)
);

CREATE TABLE performance_review (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    review_date DATE NOT NULL,
    score INTEGER,
    comments TEXT
);
