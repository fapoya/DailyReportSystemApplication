package com.techacademy.repository;

import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    Optional<Report> findById(Long id);
    List<Report> findByEmployeeCode(String employeeCode);
    List<Report> findByEmployee_code(String employeeCode);
    // 特定の従業員コードと日付で日報を検索するメソッド
    Optional<Report> findByEmployee_CodeAndReportDate(String employeeCode, LocalDate reportDate);
    List<Report> findByEmployee(Employee employee);
}

