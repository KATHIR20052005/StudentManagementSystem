package com.sms.dao;

import com.sms.exception.DatabaseException;
import com.sms.exception.DuplicateStudentException;
import com.sms.exception.ValidationException;
import com.sms.model.Student;
import com.sms.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link StudentDAO}.
 * <ul>
 *   <li>Uses {@link PreparedStatement} everywhere, which prevents SQL injection.</li>
 *   <li>Uses try-with-resources so connections/statements/result sets never leak.</li>
 *   <li>Translates vendor SQL errors into meaningful application exceptions.</li>
 * </ul>
 */
public class StudentDAOImpl implements StudentDAO {

    // MySQL vendor error codes
    private static final int ER_DUP_ENTRY = 1062;
    private static final int ER_NO_REFERENCED_ROW = 1452;

    private static final String SELECT_BASE = """
            SELECT s.student_id, s.roll_number, s.first_name, s.last_name, s.email, s.phone,
                   s.department_id, d.name AS department_name,
                   s.year_of_study, s.cgpa, s.enrollment_date
            FROM students s
            JOIN departments d ON d.department_id = s.department_id
            """;

    private static final String INSERT_SQL = """
            INSERT INTO students
                (roll_number, first_name, last_name, email, phone,
                 department_id, year_of_study, cgpa, enrollment_date)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String UPDATE_SQL = """
            UPDATE students
               SET roll_number = ?, first_name = ?, last_name = ?, email = ?, phone = ?,
                   department_id = ?, year_of_study = ?, cgpa = ?, enrollment_date = ?
             WHERE student_id = ?
            """;

    private static final String DELETE_SQL = "DELETE FROM students WHERE student_id = ?";

    @Override
    public int add(Student s) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            bindStudent(ps, s);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new DatabaseException("Student was inserted but no ID was returned.");
        } catch (SQLException e) {
            throw translate("register student", e);
        }
    }

    @Override
    public boolean update(Student s) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE_SQL)) {

            int next = bindStudent(ps, s);
            ps.setInt(next, s.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw translate("update student", e);
        }
    }

    @Override
    public boolean delete(int id) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE_SQL)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw translate("delete student", e);
        }
    }

    @Override
    public Optional<Student> findById(int id) {
        return findOne(SELECT_BASE + " WHERE s.student_id = ?", ps -> ps.setInt(1, id));
    }

    @Override
    public Optional<Student> findByRollNumber(String rollNumber) {
        return findOne(SELECT_BASE + " WHERE s.roll_number = ?", ps -> ps.setString(1, rollNumber.trim()));
    }

    @Override
    public List<Student> findAll() {
        return findMany(SELECT_BASE + " ORDER BY s.student_id", ps -> { });
    }

    @Override
    public List<Student> searchByName(String keyword) {
        String like = "%" + keyword.trim() + "%";
        return findMany(SELECT_BASE
                        + " WHERE s.first_name LIKE ? OR s.last_name LIKE ?"
                        + "    OR CONCAT(s.first_name, ' ', s.last_name) LIKE ?"
                        + " ORDER BY s.last_name, s.first_name",
                ps -> {
                    ps.setString(1, like);
                    ps.setString(2, like);
                    ps.setString(3, like);
                });
    }

    @Override
    public List<Student> findByDepartment(int departmentId) {
        return findMany(SELECT_BASE + " WHERE s.department_id = ? ORDER BY s.last_name, s.first_name",
                ps -> ps.setInt(1, departmentId));
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    @FunctionalInterface
    private interface ParamSetter {
        void accept(PreparedStatement ps) throws SQLException;
    }

    private Optional<Student> findOne(String sql, ParamSetter setter) {
        List<Student> rows = findMany(sql, setter);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    private List<Student> findMany(String sql, ParamSetter setter) {
        List<Student> result = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            setter.accept(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw translate("read students", e);
        }
    }

    /** Binds the nine data columns (1..9) and returns the next free parameter index (10). */
    private int bindStudent(PreparedStatement ps, Student s) throws SQLException {
        int i = 1;
        ps.setString(i++, s.getRollNumber().trim());
        ps.setString(i++, s.getFirstName().trim());
        ps.setString(i++, s.getLastName().trim());
        ps.setString(i++, s.getEmail().trim().toLowerCase());
        String phone = s.getPhone();
        if (phone == null || phone.isBlank()) {
            ps.setNull(i++, java.sql.Types.VARCHAR);
        } else {
            ps.setString(i++, phone.trim());
        }
        ps.setInt(i++, s.getDepartmentId());
        ps.setInt(i++, s.getYearOfStudy());
        ps.setBigDecimal(i++, java.math.BigDecimal.valueOf(s.getCgpa()).setScale(2, java.math.RoundingMode.HALF_UP));
        ps.setDate(i++, Date.valueOf(s.getEnrollmentDate()));
        return i;
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getInt("student_id"));
        s.setRollNumber(rs.getString("roll_number"));
        s.setFirstName(rs.getString("first_name"));
        s.setLastName(rs.getString("last_name"));
        s.setEmail(rs.getString("email"));
        s.setPhone(rs.getString("phone"));
        s.setDepartmentId(rs.getInt("department_id"));
        s.setDepartmentName(rs.getString("department_name"));
        s.setYearOfStudy(rs.getInt("year_of_study"));
        s.setCgpa(rs.getBigDecimal("cgpa").doubleValue());
        s.setEnrollmentDate(rs.getDate("enrollment_date").toLocalDate());
        return s;
    }

    /** Maps vendor SQL errors to application-level exceptions with clear messages. */
    private RuntimeException translate(String action, SQLException e) {
        if (e instanceof SQLIntegrityConstraintViolationException || e.getErrorCode() == ER_DUP_ENTRY
                || e.getErrorCode() == ER_NO_REFERENCED_ROW) {
            if (e.getErrorCode() == ER_DUP_ENTRY) {
                String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
                String field = msg.contains("email") ? "email" : msg.contains("roll") ? "roll number" : "value";
                return new DuplicateStudentException("A student with this " + field + " already exists.", e);
            }
            if (e.getErrorCode() == ER_NO_REFERENCED_ROW) {
                return new ValidationException("The selected department does not exist.");
            }
        }
        return new DatabaseException("Database error while trying to " + action + ": " + e.getMessage(), e);
    }
}
