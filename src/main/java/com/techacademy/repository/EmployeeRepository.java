package com.techacademy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techacademy.entity.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, String> {

    Employee findByCode(String employeeCode);

    List<Employee> findByNameLike(String string);
}