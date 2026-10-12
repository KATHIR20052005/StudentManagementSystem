package com.sms.util;

import com.sms.exception.ValidationException;
import com.sms.model.Student;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Stateless input validation for {@link Student} data.
 * Every method throws {@link ValidationException} with a user-readable message.
 */
public final class Validator {

    private static final Pattern ROLL = Pattern.compile("^[A-Za-z0-9\\-]{3,20}$");
    private static final Pattern NAME = Pattern.compile("^[A-Za-z][A-Za-z .'-]{0,49}$");
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^[0-9]{10,15}$");

    private Validator() {
    }

    public static void validate(Student s) {
        if (s == null) {
            throw new ValidationException("Student data is required.");
        }
        requireRollNumber(s.getRollNumber());
        requireName("First name", s.getFirstName());
        requireName("Last name", s.getLastName());
        requireEmail(s.getEmail());
        requirePhone(s.getPhone());
        if (s.getDepartmentId() <= 0) {
            throw new ValidationException("A valid department must be selected.");
        }
        requireYear(s.getYearOfStudy());
        requireCgpa(s.getCgpa());
        if (s.getEnrollmentDate() == null) {
            throw new ValidationException("Enrollment date is required.");
        }
        if (s.getEnrollmentDate().isAfter(LocalDate.now())) {
            throw new ValidationException("Enrollment date cannot be in the future.");
        }
    }

    public static void requireRollNumber(String roll) {
        if (roll == null || !ROLL.matcher(roll.trim()).matches()) {
            throw new ValidationException("Roll number must be 3-20 characters (letters, digits, hyphen).");
        }
    }

    public static void requireName(String label, String value) {
        if (value == null || !NAME.matcher(value.trim()).matches()) {
            throw new ValidationException(label + " must start with a letter and contain only letters, spaces, . ' -");
        }
    }

    public static void requireEmail(String email) {
        if (email == null || email.length() > 100 || !EMAIL.matcher(email.trim()).matches()) {
            throw new ValidationException("Email address is not valid.");
        }
    }

    /** Phone is optional: null or blank passes; otherwise it must be 10-15 digits. */
    public static void requirePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return;
        }
        if (!PHONE.matcher(phone.trim()).matches()) {
            throw new ValidationException("Phone must contain 10-15 digits only.");
        }
    }

    public static void requireYear(int year) {
        if (year < 1 || year > 4) {
            throw new ValidationException("Year of study must be between 1 and 4.");
        }
    }

    public static void requireCgpa(double cgpa) {
        if (Double.isNaN(cgpa) || cgpa < 0.0 || cgpa > 10.0) {
            throw new ValidationException("CGPA must be between 0.00 and 10.00.");
        }
    }
}
