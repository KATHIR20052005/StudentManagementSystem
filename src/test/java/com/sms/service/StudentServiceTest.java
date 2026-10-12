package com.sms.service;

import com.sms.dao.DepartmentDAO;
import com.sms.dao.StudentDAO;
import com.sms.exception.DuplicateStudentException;
import com.sms.exception.StudentNotFoundException;
import com.sms.exception.ValidationException;
import com.sms.model.Department;
import com.sms.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exercises the service layer against in-memory fake DAOs, proving the business
 * logic works independently of MySQL (a benefit of the DAO-interface design).
 */
class StudentServiceTest {

    /** Minimal in-memory StudentDAO. */
    static class FakeStudentDAO implements StudentDAO {
        final Map<Integer, Student> store = new LinkedHashMap<>();
        int nextId = 1;

        @Override public int add(Student s) {
            s.setId(nextId++);
            s.setDepartmentName("Computer Science and Engineering");
            store.put(s.getId(), s);
            return s.getId();
        }
        @Override public boolean update(Student s) {
            if (!store.containsKey(s.getId())) return false;
            s.setDepartmentName("Computer Science and Engineering");
            store.put(s.getId(), s);
            return true;
        }
        @Override public boolean delete(int id) { return store.remove(id) != null; }
        @Override public Optional<Student> findById(int id) { return Optional.ofNullable(store.get(id)); }
        @Override public Optional<Student> findByRollNumber(String roll) {
            return store.values().stream().filter(s -> s.getRollNumber().equalsIgnoreCase(roll.trim())).findFirst();
        }
        @Override public List<Student> findAll() { return new ArrayList<>(store.values()); }
        @Override public List<Student> searchByName(String k) {
            String q = k.toLowerCase();
            return store.values().stream()
                    .filter(s -> s.getFullName().toLowerCase().contains(q)).toList();
        }
        @Override public List<Student> findByDepartment(int d) {
            return store.values().stream().filter(s -> s.getDepartmentId() == d).toList();
        }
    }

    /** Department 1 exists; everything else does not. */
    static class FakeDepartmentDAO implements DepartmentDAO {
        final Department cse = new Department(1, "CSE", "Computer Science and Engineering");
        @Override public List<Department> findAll() { return List.of(cse); }
        @Override public Optional<Department> findById(int id) { return id == 1 ? Optional.of(cse) : Optional.empty(); }
    }

    private StudentService service;

    @BeforeEach
    void setUp() {
        service = new StudentService(new FakeStudentDAO(), new FakeDepartmentDAO());
    }

    private Student sample(String roll, String email) {
        return new Student(roll, "Aarav", "Sharma", email, "9876543210", 1, 2, 8.5, LocalDate.of(2024, 8, 1));
    }

    @Test
    void registerAssignsIdAndReturnsRecord() {
        Student saved = service.register(sample("CSE001", "a@example.com"));
        assertEquals(1, saved.getId());
        assertEquals("CSE001", saved.getRollNumber());
    }

    @Test
    void registerRejectsDuplicateRollNumber() {
        service.register(sample("CSE001", "a@example.com"));
        assertThrows(DuplicateStudentException.class, () -> service.register(sample("CSE001", "b@example.com")));
    }

    @Test
    void registerRejectsUnknownDepartment() {
        Student s = sample("CSE001", "a@example.com");
        s.setDepartmentId(99);
        assertThrows(ValidationException.class, () -> service.register(s));
    }

    @Test
    void registerRejectsInvalidData() {
        Student s = sample("CSE001", "bad-email");
        assertThrows(ValidationException.class, () -> service.register(s));
    }

    @Test
    void updateChangesFields() {
        Student saved = service.register(sample("CSE001", "a@example.com"));
        saved.setCgpa(9.25);
        Student updated = service.update(saved);
        assertEquals(9.25, updated.getCgpa(), 0.001);
    }

    @Test
    void updateCannotStealAnotherStudentsRollNumber() {
        service.register(sample("CSE001", "a@example.com"));
        Student second = service.register(sample("CSE002", "b@example.com"));
        second.setRollNumber("CSE001");
        assertThrows(DuplicateStudentException.class, () -> service.update(second));
    }

    @Test
    void updateMissingStudentThrowsNotFound() {
        Student ghost = sample("CSE777", "ghost@example.com");
        ghost.setId(42);
        assertThrows(StudentNotFoundException.class, () -> service.update(ghost));
    }

    @Test
    void deleteRemovesStudent() {
        Student saved = service.register(sample("CSE001", "a@example.com"));
        service.delete(saved.getId());
        assertThrows(StudentNotFoundException.class, () -> service.getById(saved.getId()));
    }

    @Test
    void deleteMissingStudentThrowsNotFound() {
        assertThrows(StudentNotFoundException.class, () -> service.delete(123));
    }

    @Test
    void searchByNameFindsPartialMatches() {
        service.register(sample("CSE001", "a@example.com"));
        assertEquals(1, service.searchByName("arav").size());
        assertTrue(service.searchByName("zzz").isEmpty());
    }

    @Test
    void searchRejectsBlankKeyword() {
        assertThrows(ValidationException.class, () -> service.searchByName("   "));
    }

    @Test
    void getByRollNumberNotFound() {
        assertThrows(StudentNotFoundException.class, () -> service.getByRollNumber("NOPE123"));
    }
}
