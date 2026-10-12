package com.sms.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Domain entity representing one student's academic record.
 * Pure data holder: no database or UI logic lives here.
 */
public class Student {

    private int id;
    private String rollNumber;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private int departmentId;
    private String departmentName; // populated by JOIN when reading
    private int yearOfStudy;
    private double cgpa;
    private LocalDate enrollmentDate;

    public Student() {
    }

    public Student(String rollNumber, String firstName, String lastName, String email, String phone,
                   int departmentId, int yearOfStudy, double cgpa, LocalDate enrollmentDate) {
        this.rollNumber = rollNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.departmentId = departmentId;
        this.yearOfStudy = yearOfStudy;
        this.cgpa = cgpa;
        this.enrollmentDate = enrollmentDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public int getDepartmentId() { return departmentId; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public int getYearOfStudy() { return yearOfStudy; }
    public void setYearOfStudy(int yearOfStudy) { this.yearOfStudy = yearOfStudy; }

    public double getCgpa() { return cgpa; }
    public void setCgpa(double cgpa) { this.cgpa = cgpa; }

    public LocalDate getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(LocalDate enrollmentDate) { this.enrollmentDate = enrollmentDate; }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student other)) return false;
        return id == other.id && Objects.equals(rollNumber, other.rollNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, rollNumber);
    }

    @Override
    public String toString() {
        return String.format("Student{id=%d, roll=%s, name=%s, dept=%s, year=%d, cgpa=%.2f}",
                id, rollNumber, getFullName(), departmentName, yearOfStudy, cgpa);
    }
}
