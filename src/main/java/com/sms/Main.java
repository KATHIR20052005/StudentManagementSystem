package com.sms;

import com.sms.dao.DepartmentDAOImpl;
import com.sms.dao.StudentDAOImpl;
import com.sms.exception.DatabaseException;
import com.sms.exception.DuplicateStudentException;
import com.sms.exception.StudentNotFoundException;
import com.sms.exception.ValidationException;
import com.sms.model.Department;
import com.sms.model.Student;
import com.sms.service.StudentService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Console user interface. Contains no SQL and no business rules:
 * it collects input, calls {@link StudentService}, and prints results.
 */
public class Main {

    private static final Scanner IN = new Scanner(System.in);
    private static final StudentService SERVICE =
            new StudentService(new StudentDAOImpl(), new DepartmentDAOImpl());

    public static void main(String[] args) {
        System.out.println("=====================================");
        System.out.println("      STUDENT MANAGEMENT SYSTEM      ");
        System.out.println("=====================================");

        boolean running = true;
        while (running) {
            printMenu();
            String choice = prompt("Choose an option");
            try {
                switch (choice) {
                    case "1" -> registerStudent();
                    case "2" -> viewAll();
                    case "3" -> searchByRoll();
                    case "4" -> searchByName();
                    case "5" -> updateStudent();
                    case "6" -> deleteStudent();
                    case "7" -> listDepartments();
                    case "8" -> studentsByDepartment();
                    case "0" -> {
                        running = false;
                        System.out.println("Goodbye!");
                    }
                    default -> System.out.println("Invalid option. Please choose 0-8.");
                }
            } catch (ValidationException | DuplicateStudentException | StudentNotFoundException e) {
                System.out.println("[!] " + e.getMessage());
            } catch (DatabaseException e) {
                System.out.println("[DB ERROR] " + e.getMessage());
            } catch (RuntimeException e) {
                System.out.println("[UNEXPECTED ERROR] " + e.getMessage());
            }
        }
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("1. Register new student");
        System.out.println("2. View all students");
        System.out.println("3. Search by roll number");
        System.out.println("4. Search by name");
        System.out.println("5. Update student");
        System.out.println("6. Delete student");
        System.out.println("7. List departments");
        System.out.println("8. View students by department");
        System.out.println("0. Exit");
    }

    // ------------------------------------------------------------------
    // Menu actions
    // ------------------------------------------------------------------

    private static void registerStudent() {
        System.out.println("\n--- Register Student ---");
        Student s = new Student();
        s.setRollNumber(prompt("Roll number"));
        s.setFirstName(prompt("First name"));
        s.setLastName(prompt("Last name"));
        s.setEmail(prompt("Email"));
        s.setPhone(prompt("Phone (optional, press Enter to skip)"));
        listDepartments();
        s.setDepartmentId(promptInt("Department ID"));
        s.setYearOfStudy(promptInt("Year of study (1-4)"));
        s.setCgpa(promptDouble("CGPA (0-10)"));
        s.setEnrollmentDate(promptDate("Enrollment date (yyyy-MM-dd)"));

        Student saved = SERVICE.register(s);
        System.out.println("Student registered successfully with ID " + saved.getId() + ".");
        printStudent(saved);
    }

    private static void viewAll() {
        List<Student> students = SERVICE.getAll();
        printTable(students);
    }

    private static void searchByRoll() {
        String roll = prompt("Enter roll number");
        printStudent(SERVICE.getByRollNumber(roll));
    }

    private static void searchByName() {
        String keyword = prompt("Enter name (or part of it)");
        printTable(SERVICE.searchByName(keyword));
    }

    private static void updateStudent() {
        int id = promptInt("Enter ID of the student to update");
        Student s = SERVICE.getById(id);
        System.out.println("Current record:");
        printStudent(s);
        System.out.println("Press Enter to keep the current value.");

        String v = prompt("Roll number [" + s.getRollNumber() + "]");
        if (!v.isEmpty()) s.setRollNumber(v);

        v = prompt("First name [" + s.getFirstName() + "]");
        if (!v.isEmpty()) s.setFirstName(v);

        v = prompt("Last name [" + s.getLastName() + "]");
        if (!v.isEmpty()) s.setLastName(v);

        v = prompt("Email [" + s.getEmail() + "]");
        if (!v.isEmpty()) s.setEmail(v);

        v = prompt("Phone [" + (s.getPhone() == null ? "none" : s.getPhone()) + "]");
        if (!v.isEmpty()) s.setPhone(v);

        listDepartments();
        v = prompt("Department ID [" + s.getDepartmentId() + "]");
        if (!v.isEmpty()) s.setDepartmentId(parseInt(v, "Department ID"));

        v = prompt("Year of study [" + s.getYearOfStudy() + "]");
        if (!v.isEmpty()) s.setYearOfStudy(parseInt(v, "Year of study"));

        v = prompt(String.format("CGPA [%.2f]", s.getCgpa()));
        if (!v.isEmpty()) s.setCgpa(parseDouble(v, "CGPA"));

        v = prompt("Enrollment date [" + s.getEnrollmentDate() + "]");
        if (!v.isEmpty()) s.setEnrollmentDate(parseDate(v));

        Student updated = SERVICE.update(s);
        System.out.println("Student updated successfully.");
        printStudent(updated);
    }

    private static void deleteStudent() {
        int id = promptInt("Enter ID of the student to delete");
        Student s = SERVICE.getById(id);
        printStudent(s);
        String confirm = prompt("Type YES to permanently delete this student");
        if (confirm.equals("YES")) {
            SERVICE.delete(id);
            System.out.println("Student deleted.");
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    private static void listDepartments() {
        System.out.println("\nDepartments:");
        for (Department d : SERVICE.getDepartments()) {
            System.out.println("  " + d);
        }
    }

    private static void studentsByDepartment() {
        listDepartments();
        int deptId = promptInt("Department ID");
        printTable(SERVICE.getByDepartment(deptId));
    }

    // ------------------------------------------------------------------
    // Output helpers
    // ------------------------------------------------------------------

    private static void printStudent(Student s) {
        System.out.println("-------------------------------------");
        System.out.println("ID          : " + s.getId());
        System.out.println("Roll number : " + s.getRollNumber());
        System.out.println("Name        : " + s.getFullName());
        System.out.println("Email       : " + s.getEmail());
        System.out.println("Phone       : " + (s.getPhone() == null ? "-" : s.getPhone()));
        System.out.println("Department  : " + s.getDepartmentName());
        System.out.println("Year        : " + s.getYearOfStudy());
        System.out.printf("CGPA        : %.2f%n", s.getCgpa());
        System.out.println("Enrolled on : " + s.getEnrollmentDate());
        System.out.println("-------------------------------------");
    }

    private static void printTable(List<Student> students) {
        if (students.isEmpty()) {
            System.out.println("No students found.");
            return;
        }
        String fmt = "%-4s %-13s %-22s %-8s %-5s %-6s%n";
        System.out.printf(fmt, "ID", "Roll No", "Name", "Dept", "Year", "CGPA");
        System.out.println("-".repeat(64));
        for (Student s : students) {
            System.out.printf(fmt, s.getId(), s.getRollNumber(), truncate(s.getFullName(), 22),
                    deptCode(s.getDepartmentName()), s.getYearOfStudy(), String.format("%.2f", s.getCgpa()));
        }
        System.out.println(students.size() + " record(s).");
    }

    private static String deptCode(String name) {
        // Lightweight display helper: first letters of each word, e.g. "Computer Science" -> "CS"
        if (name == null || name.isBlank()) return "-";
        StringBuilder sb = new StringBuilder();
        for (String w : name.split("\\s+")) {
            if (!w.equalsIgnoreCase("and") && !w.isEmpty()) sb.append(Character.toUpperCase(w.charAt(0)));
        }
        return sb.length() > 8 ? sb.substring(0, 8) : sb.toString();
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + ".";
    }

    // ------------------------------------------------------------------
    // Input helpers (never throw on bad numeric/date input from a prompt loop)
    // ------------------------------------------------------------------

    private static String prompt(String label) {
        System.out.print(label + ": ");
        if (!IN.hasNextLine()) {
            // stdin closed (Ctrl-D / end of piped input) - exit cleanly
            System.out.println("\nInput ended. Goodbye!");
            System.exit(0);
        }
        return IN.nextLine().trim();
    }

    private static int promptInt(String label) {
        return parseInt(prompt(label), label);
    }

    private static double promptDouble(String label) {
        return parseDouble(prompt(label), label);
    }

    private static LocalDate promptDate(String label) {
        return parseDate(prompt(label));
    }

    private static int parseInt(String text, String label) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new ValidationException(label + " must be a whole number.");
        }
    }

    private static double parseDouble(String text, String label) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new ValidationException(label + " must be a number.");
        }
    }

    private static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Date must be in yyyy-MM-dd format (e.g. 2024-08-01).");
        }
    }
}
