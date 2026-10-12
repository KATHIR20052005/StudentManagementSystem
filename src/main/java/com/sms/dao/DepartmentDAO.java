package com.sms.dao;

import com.sms.model.Department;

import java.util.List;
import java.util.Optional;

public interface DepartmentDAO {

    List<Department> findAll();

    Optional<Department> findById(int id);
}
