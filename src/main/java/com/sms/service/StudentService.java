package com.sms.service;

import com.sms.dao.DepartmentDAO;
import com.sms.dao.StudentDAO;
import com.sms.exception.DuplicateStudentException;
import com.sms.exception.StudentNotFoundException;
import com.sms.exception.ValidationException;
import com.sms.model.Department;
import com.sms.model.Student;
import com.sms.util.Validator;

import java.util.List;

/**
 * Business-logic layer. Validates input, enforces rules, and coordinates the DAOs.
 * The UI talks only to this class.
 */
public class StudentService {

    private final StudentDAO studentDAO;
    private final DepartmentDAO departmentDAO;

    public StudentService(StudentDAO studentDAO, DepartmentDAO departmentDAO) {
        this.studentDAO = studentDAO;
        this.departmentDAO = departmentDAO;
    }

    /** Registers a new student and returns the stored record (with generated ID). */
    public Student register(Student student) {
        Validator.validate(student);
        ensureDepartmentExists(student.getDepartmentId());

        if (studentDAO.findByRollNumber(student.getRollNumber()).isPresent()) {
            throw new DuplicateStudentException(
                    "A student with roll number '" + student.getRollNumber() + "' already exists.", null);
        }
        int id = studentDAO.add(student);
        return getById(id);
    }

    public Student update(Student student) {
        if (student.getId() <= 0) {
            throw new ValidationException("A valid student ID is required for update.");
        }
        Validator.validate(student);
        ensureDepartmentExists(student.getDepartmentId());

        // Roll number must stay unique across *other* students
        studentDAO.findByRollNumber(student.getRollNumber()).ifPresent(other -> {
            if (other.getId() != student.getId()) {
                throw new DuplicateStudentException(
                        "Roll number '" + student.getRollNumber() + "' belongs to another student.", null);
            }
        });

        if (!studentDAO.update(student)) {
            throw new StudentNotFoundException("No student found with ID " + student.getId());
        }
        return getById(student.getId());
    }

    public void delete(int id) {
        if (!studentDAO.delete(id)) {
            throw new StudentNotFoundException("No student found with ID " + id);
        }
    }

    public Student getById(int id) {
        return studentDAO.findById(id)
                .orElseThrow(() -> new StudentNotFoundException("No student found with ID " + id));
    }

    public Student getByRollNumber(String rollNumber) {
        Validator.requireRollNumber(rollNumber);
        return studentDAO.findByRollNumber(rollNumber)
                .orElseThrow(() -> new StudentNotFoundException("No student found with roll number " + rollNumber));
    }

    public List<Student> getAll() {
        return studentDAO.findAll();
    }

    public List<Student> searchByName(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new ValidationException("Search keyword cannot be empty.");
        }
        return studentDAO.searchByName(keyword);
    }

    public List<Student> getByDepartment(int departmentId) {
        ensureDepartmentExists(departmentId);
        return studentDAO.findByDepartment(departmentId);
    }

    public List<Department> getDepartments() {
        return departmentDAO.findAll();
    }

    private void ensureDepartmentExists(int departmentId) {
        if (departmentDAO.findById(departmentId).isEmpty()) {
            throw new ValidationException("Department with ID " + departmentId + " does not exist.");
        }
    }
}
