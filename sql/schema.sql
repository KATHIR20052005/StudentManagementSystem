

CREATE DATABASE IF NOT EXISTS student_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE student_db;

DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS departments;


CREATE TABLE departments (
    department_id INT          NOT NULL AUTO_INCREMENT,
    code          VARCHAR(10)  NOT NULL,
    name          VARCHAR(100) NOT NULL,
    PRIMARY KEY (department_id),
    UNIQUE KEY uq_departments_code (code),
    UNIQUE KEY uq_departments_name (name)
) ENGINE=InnoDB;


--CRUD

CREATE TABLE students (
    student_id      INT           NOT NULL AUTO_INCREMENT,
    roll_number     VARCHAR(20)   NOT NULL,
    first_name      VARCHAR(50)   NOT NULL,
    last_name       VARCHAR(50)   NOT NULL,
    email           VARCHAR(100)  NOT NULL,
    phone           VARCHAR(15)       NULL,
    department_id   INT           NOT NULL,
    year_of_study   TINYINT       NOT NULL,
    cgpa            DECIMAL(4,2)  NOT NULL DEFAULT 0.00,
    enrollment_date DATE          NOT NULL,
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (student_id),
    UNIQUE KEY uq_students_roll_number (roll_number),
    UNIQUE KEY uq_students_email (email),
    KEY idx_students_name (last_name, first_name),
    KEY idx_students_department (department_id),
    CONSTRAINT fk_students_department
        FOREIGN KEY (department_id) REFERENCES departments (department_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT chk_students_year CHECK (year_of_study BETWEEN 1 AND 4),
    CONSTRAINT chk_students_cgpa CHECK (cgpa BETWEEN 0.00 AND 10.00)
) ENGINE=InnoDB;


INSERT INTO departments (code, name) VALUES
    ('CSE',  'Computer Science and Engineering'),
    ('ECE',  'Electronics and Communication Engineering'),
    ('MECH', 'Mechanical Engineering'),
    ('AERO', 'Aerospace Engineering'),
    ('CIVIL','Civil Engineering');

INSERT INTO students
    (roll_number, first_name, last_name, email, phone, department_id, year_of_study, cgpa, enrollment_date)
VALUES
    ('CSE2024001',  'Aarav',  'Sharma', 'aarav.sharma@example.com', '9876543210', 1, 2, 8.75, '2024-08-01'),
    ('ECE2024002',  'Meera',  'Nair',   'meera.nair@example.com',   '9123456780', 2, 3, 9.10, '2023-08-01'),
    ('AERO2024003', 'Rohan',  'Verma',  'rohan.verma@example.com',  '9988776655', 4, 1, 8.20, '2024-08-01');
