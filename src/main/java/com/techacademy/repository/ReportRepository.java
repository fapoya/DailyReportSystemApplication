package com.techacademy.repository;

import com.techacademy.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByEmployee_Code(String code);
    @Query("SELECT r FROM Report r WHERE r.employee.code = :code")
    List<Report> findByEmployeeCode(@Param("code") String code);
    Optional<Report> findById(Long id);
}