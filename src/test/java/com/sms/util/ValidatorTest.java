package com.sms.util;

import com.sms.exception.ValidationException;
import com.sms.model.Student;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidatorTest {

    private Student valid() {
        return new Student("CSE2024001", "Aarav", "Sharma", "aarav@example.com", "9876543210",
                1, 2, 8.5, LocalDate.of(2024, 8, 1));
    }

    @Test
    void validStudentPasses() {
        assertDoesNotThrow(() -> Validator.validate(valid()));
    }

    @Test
    void phoneIsOptional() {
        Student s = valid();
        s.setPhone("");
        assertDoesNotThrow(() -> Validator.validate(s));
        s.setPhone(null);
        assertDoesNotThrow(() -> Validator.validate(s));
    }

    @Test
    void rejectsBadRollNumber() {
        Student s = valid();
        s.setRollNumber("a b");
        assertThrows(ValidationException.class, () -> Validator.validate(s));
    }

    @Test
    void rejectsBadEmail() {
        Student s = valid();
        s.setEmail("not-an-email");
        assertThrows(ValidationException.class, () -> Validator.validate(s));
    }

    @Test
    void rejectsBadPhone() {
        Student s = valid();
        s.setPhone("12ab");
        assertThrows(ValidationException.class, () -> Validator.validate(s));
    }

    @Test
    void rejectsNamesWithDigits() {
        Student s = valid();
        s.setFirstName("R2D2");
        assertThrows(ValidationException.class, () -> Validator.validate(s));
    }

    @Test
    void rejectsYearOutOfRange() {
        Student s = valid();
        s.setYearOfStudy(0);
        assertThrows(ValidationException.class, () -> Validator.validate(s));
        s.setYearOfStudy(5);
        assertThrows(ValidationException.class, () -> Validator.validate(s));
    }

    @Test
    void rejectsCgpaOutOfRange() {
        Student s = valid();
        s.setCgpa(10.5);
        assertThrows(ValidationException.class, () -> Validator.validate(s));
        s.setCgpa(-0.1);
        assertThrows(ValidationException.class, () -> Validator.validate(s));
    }

    @Test
    void rejectsFutureEnrollmentDate() {
        Student s = valid();
        s.setEnrollmentDate(LocalDate.now().plusDays(1));
        assertThrows(ValidationException.class, () -> Validator.validate(s));
    }

    @Test
    void rejectsMissingDepartment() {
        Student s = valid();
        s.setDepartmentId(0);
        assertThrows(ValidationException.class, () -> Validator.validate(s));
    }
}
