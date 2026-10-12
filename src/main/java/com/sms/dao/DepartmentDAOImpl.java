package com.sms.dao;

import com.sms.exception.DatabaseException;
import com.sms.model.Department;
import com.sms.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DepartmentDAOImpl implements DepartmentDAO {

    @Override
    public List<Department> findAll() {
        List<Department> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT department_id, code, name FROM departments ORDER BY department_id")) {
            while (rs.next()) {
                list.add(new Department(rs.getInt(1), rs.getString(2), rs.getString(3)));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Database error while reading departments: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Department> findById(int id) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT department_id, code, name FROM departments WHERE department_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Department(rs.getInt(1), rs.getString(2), rs.getString(3)));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error while reading department: " + e.getMessage(), e);
        }
    }
}
