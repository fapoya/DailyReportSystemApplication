package com.techacademy.repository;

import com.techacademy.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, String> {

    List<Report> findByEmployeeCode(String employeeCode);
 // ReportRepository内
}