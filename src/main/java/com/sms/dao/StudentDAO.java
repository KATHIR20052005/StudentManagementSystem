package com.sms.dao;

import com.sms.model.Student;

import java.util.List;
import java.util.Optional;

/**
 * Data-access contract for students. The service layer depends on this interface,
 * not on JDBC, so the persistence mechanism can be swapped or mocked in tests.
 */
public interface StudentDAO {

    /** Inserts a student and returns the generated primary key. */
    int add(Student student);

    /** Updates the student identified by {@code student.getId()}; returns true if a row changed. */
    boolean update(Student student);

    /** Deletes by primary key; returns true if a row was removed. */
    boolean delete(int id);

    Optional<Student> findById(int id);

    Optional<Student> findByRollNumber(String rollNumber);

    List<Student> findAll();

    /** Case-insensitive partial match on first or last name. */
    List<Student> searchByName(String keyword);

    List<Student> findByDepartment(int departmentId);
}
