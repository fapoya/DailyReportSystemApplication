package com.techacademy.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.constants.ErrorMessage;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.repository.EmployeeRepository;
import com.techacademy.repository.ReportRepository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import lombok.Data;

@Service
@Data
public class ReportService {

    private final ReportRepository reportRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    public ReportService(ReportRepository reportRepository, PasswordEncoder passwordEncoder) {
        this.reportRepository = reportRepository;
    }

    public void addReport(String employeeCode, Report report) {
        // 特定の従業員コードに基づいてEmployeeオブジェクトを検索
        Employee employee = employeeRepository.findByCode(employeeCode);

        if (employee != null) {
            // ReportオブジェクトにEmployeeオブジェクトを設定
            report.setEmployee(employee);

            // Reportオブジェクトをデータベースに保存
            reportRepository.save(report);
        }
    }

    // 日報保存
        @Transactional
        public ErrorKinds save(Report report) {

            report.setDeleteFlg(false);

            LocalDateTime now = LocalDateTime.now();
            report.setCreatedAt(now);
            report.setUpdatedAt(now);

            reportRepository.save(report);
            return ErrorKinds.SUCCESS;
        }


        // 更新（追加）を行なう
        @Transactional
        public ErrorKinds update(Long id, Report updatedReport) {
            Optional<Report> existingReportOpt = reportRepository.findById(id);
            if (!existingReportOpt.isPresent()) {
                return ErrorKinds.BLANK_ERROR; // レポートが見つからない場合のエラー処理
            }
            Report existingReport = existingReportOpt.get();

            // 既存のcreated_atの値を新しいReportオブジェクトに設定
            updatedReport.setCreatedAt(existingReport.getCreatedAt());

            // 更新処理でupdatedAtのみを現在時刻に設定
            updatedReport.setUpdatedAt(LocalDateTime.now());

            // リポジトリを使用してデータベースに保存
            reportRepository.save(updatedReport);

            return ErrorKinds.SUCCESS;
        }

        // ロールに基づいて日報を取得
        public List<Report> findReportsByUserIdAndRole(String id, String role) {
            if ("ADMIN".equals(role)) {
                // 管理者の場合、全ての日報を取得
                return reportRepository.findAll();
            } else {
                // 一般ユーザーの場合、そのユーザーの日報のみを取得
                // findByIdで検索
                return reportRepository.findByEmployeeCode(id);
            }
        }

         // 1件を検索
        public Report findByCode(Long id) {
            // findByIdで検索
            Optional<Report> option = Optional.ofNullable(reportRepository.findById(id).get());
            // 取得できなかった場合はnullを返す
            Report report = option.orElse(null);
            return report;
        }

        /** 日報の登録を行なう */
        @Transactional
        public Report saveReport(Report report) {
            report.setDeleteFlg(false);

            LocalDateTime now = LocalDateTime.now();
            report.setCreatedAt(now);
            report.setUpdatedAt(now);

            reportRepository.save(report);
            return reportRepository.save(report);

        }

}
