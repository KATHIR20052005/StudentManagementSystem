# Student Management System

A console-based Java application for managing student academic records with full **CRUD** (register, update, search, delete), backed by a **MySQL** relational database through **JDBC**.

**Tech stack:** Java 17 · JDBC · MySQL 8 · Maven · JUnit 5

## Features

- Register, update, delete and view students
- Search by roll number or by (partial) name
- Filter students by department
- Relational schema: `students` linked to `departments` by a foreign key
- Input validation (roll number, name, email, phone, year, CGPA, dates)
- Duplicate detection for roll number and email
- Delete confirmation prompt
- Credentials configurable through environment variables

## Architecture

Layered design, each layer depends only on the one below it:

```
Main (console UI)
   |
StudentService        <- validation + business rules
   |
StudentDAO / DepartmentDAO (interfaces)
   |
StudentDAOImpl / DepartmentDAOImpl   <- JDBC, PreparedStatements
   |
MySQL (student_db)
```

```
src/main/java/com/sms
├── Main.java                     console menu (no SQL, no business rules)
├── model/                        Student, Department
├── dao/                          DAO interfaces + JDBC implementations
├── service/StudentService.java   business logic
├── exception/                    ValidationException, DuplicateStudentException,
│                                 StudentNotFoundException, DatabaseException
└── util/                         DBConnection, Validator
sql/schema.sql                    tables, constraints, seed data
src/test/java                     unit tests (no database required)
```

### Design choices worth mentioning in interviews

- **DAO pattern with interfaces**: the service layer never touches JDBC, so it is unit-tested with in-memory fake DAOs.
- **`PreparedStatement` everywhere**: prevents SQL injection.
- **try-with-resources**: connections, statements and result sets are always closed.
- **Exception translation**: MySQL error 1062 (duplicate key) becomes `DuplicateStudentException`; 1452 (bad foreign key) becomes `ValidationException`; any other `SQLException` becomes an unchecked `DatabaseException`. The UI shows friendly messages and never crashes.
- **Database-level integrity**: `UNIQUE`, `FOREIGN KEY`, and `CHECK` constraints back up the Java validation.

## Setup

**Prerequisites:** JDK 17+, Maven 3.8+, MySQL 8+

1. Create the database and seed data:
   ```bash
   mysql -u root -p < sql/schema.sql
   ```
2. Set your credentials, either by editing `src/main/resources/db.properties` or with environment variables (recommended):
   ```bash
   export DB_URL="jdbc:mysql://localhost:3306/student_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
   export DB_USER=root
   export DB_PASSWORD=your_password
   ```
3. Build and run:
   ```bash
   mvn clean package
   java -jar target/student-management-system-1.0.0.jar
   ```
   or during development: `mvn compile exec:java`

4. Run the tests: `mvn test`

## Sample session

```
=====================================
      STUDENT MANAGEMENT SYSTEM
=====================================

1. Register new student
2. View all students
...
Choose an option: 2
ID   Roll No       Name                   Dept     Year  CGPA
----------------------------------------------------------------
1    CSE2024001    Aarav Sharma           CSE      2     8.75
2    ECE2024002    Meera Nair             ECE      3     9.10
3    AERO2024003   Rohan Verma            AE       1     8.20
3 record(s).
```

## Database schema

| Table | Key columns |
|-------|-------------|
| `departments` | `department_id` (PK), `code` (unique), `name` (unique) |
| `students` | `student_id` (PK), `roll_number` (unique), `email` (unique), `department_id` (FK), `year_of_study` (1-4), `cgpa` (0-10), `enrollment_date` |

## Ideas for extension

- Add a `courses` / `marks` table (many-to-many) and per-student grade reports
- Swing or JavaFX GUI, or a Spring Boot REST API on top of the existing service layer
- Connection pooling with HikariCP
- Export records to CSV

## Resume bullet

> **Student Management System** | Java, MySQL, JDBC | 2024
> - Built an object-oriented Java application with full CRUD operations (register, update, search, delete) for student academic records.
> - Connected the Java backend to a normalized MySQL database via JDBC using the DAO pattern and prepared statements, with robust exception handling and a clean layered architecture (UI, service, DAO).
> - Added input validation, duplicate detection, and unit tests for the service layer.
