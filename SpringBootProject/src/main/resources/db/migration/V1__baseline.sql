CREATE TABLE employee (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    hire_date DATE NOT NULL,
    tenant_id INTEGER NOT NULL
);

CREATE TABLE role (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255)
);

CREATE TABLE attendance (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    date DATE NOT NULL,
    check_in TIMESTAMP,
    check_out TIMESTAMP,
    status VARCHAR(20)
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
    type VARCHAR(50),
    status VARCHAR(20)
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
    description TEXT,
    incident_date DATE NOT NULL,
    severity VARCHAR(20)
);

CREATE TABLE asset (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    assigned_to INTEGER REFERENCES employee(id)
);

CREATE TABLE performance_review (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    review_date DATE NOT NULL,
    score INTEGER,
    comments TEXT
);

CREATE TABLE audit_log (
    id SERIAL PRIMARY KEY,
    action VARCHAR(100) NOT NULL,
    performed_by INTEGER REFERENCES employee(id),
    timestamp TIMESTAMP NOT NULL,
    details TEXT
);

CREATE TABLE notification (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    message TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    read BOOLEAN DEFAULT FALSE
);

CREATE TABLE announcement (
    id SERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE onboarding_task (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    description TEXT NOT NULL,
    due_date DATE,
    completed BOOLEAN DEFAULT FALSE
);

CREATE TABLE offboarding_task (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    description TEXT NOT NULL,
    due_date DATE,
    completed BOOLEAN DEFAULT FALSE
);

CREATE TABLE tenant (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    contact_email VARCHAR(100)
);

CREATE TABLE schedule_optimization (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    optimization_date DATE NOT NULL,
    details TEXT
);

CREATE TABLE self_service_portal (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employee(id),
    access_date DATE NOT NULL,
    action VARCHAR(100)
);
