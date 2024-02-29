package com.techacademy.repository;

import com.techacademy.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, String> {

    List<Report> findByEmployeeCode(String id);
 // ReportRepository内

}