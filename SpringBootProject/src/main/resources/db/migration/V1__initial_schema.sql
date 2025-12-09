-- Flyway migration script: Initial schema for Warehouse EMS

CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE security_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE security_user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES security_user(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE employee (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(20),
    hire_date DATE NOT NULL,
    position VARCHAR(100) NOT NULL,
    department VARCHAR(100),
    status VARCHAR(50) NOT NULL,
    supervisor_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (supervisor_id) REFERENCES employee(id)
);

CREATE TABLE attendance_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    clock_in TIMESTAMP,
    clock_out TIMESTAMP,
    status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employee(id)
);

CREATE TABLE shift_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    description VARCHAR(255)
);

CREATE TABLE shift_assignment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    shift_template_id BIGINT NOT NULL,
    shift_date DATE NOT NULL,
    assigned_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employee(id),
    FOREIGN KEY (shift_template_id) REFERENCES shift_template(id),
    FOREIGN KEY (assigned_by) REFERENCES security_user(id)
);

CREATE TABLE leave_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employee(id)
);

CREATE TABLE certification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    issued_by VARCHAR(100),
    issue_date DATE,
    expiry_date DATE,
    status VARCHAR(50),
    FOREIGN KEY (employee_id) REFERENCES employee(id)
);

CREATE TABLE safety_incident (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT,
    incident_date DATE NOT NULL,
    description VARCHAR(255) NOT NULL,
    severity VARCHAR(50),
    reported_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employee(id),
    FOREIGN KEY (reported_by) REFERENCES security_user(id)
);

CREATE TABLE asset (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    serial_number VARCHAR(100) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL,
    purchase_date DATE,
    assigned_to BIGINT,
    FOREIGN KEY (assigned_to) REFERENCES employee(id)
);

CREATE TABLE asset_assignment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    asset_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    assigned_date DATE NOT NULL,
    returned_date DATE,
    assigned_by BIGINT,
    FOREIGN KEY (asset_id) REFERENCES asset(id),
    FOREIGN KEY (employee_id) REFERENCES employee(id),
    FOREIGN KEY (assigned_by) REFERENCES security_user(id)
);

CREATE TABLE performance_review (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    reviewer_id BIGINT NOT NULL,
    review_date DATE NOT NULL,
    score INT NOT NULL,
    comments VARCHAR(255),
    FOREIGN KEY (employee_id) REFERENCES employee(id),
    FOREIGN KEY (reviewer_id) REFERENCES security_user(id)
);

CREATE TABLE audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    action VARCHAR(100) NOT NULL,
    entity VARCHAR(100) NOT NULL,
    entity_id BIGINT,
    performed_by BIGINT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    details VARCHAR(255),
    FOREIGN KEY (performed_by) REFERENCES security_user(id)
);