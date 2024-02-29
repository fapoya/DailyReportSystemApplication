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

@Service
public class ReportService {

    private final ReportRepository reportRepository;

    @Autowired
    public ReportService(ReportRepository reportRepository, PasswordEncoder passwordEncoder) {
        this.reportRepository = reportRepository;
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
        public ErrorKinds update(String EmployeeCode, Report updatedReport) {
            Optional<Report> existingReportOpt = reportRepository.findById(EmployeeCode);
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
        public List<Report> findReportsByUserIdAndRole(String EmployeeCode, String role) {
            if ("ADMIN".equals(role)) {
                // 管理者の場合、全ての日報を取得
                return reportRepository.findAll();
            } else {
                // 一般ユーザーの場合、そのユーザーの日報のみを取得
                // findByIdで検索
                return reportRepository.findByEmployeeCode(EmployeeCode);
            }
        }
        // 1件を検索
        public Report findByCode(String employeeCode) {
            // findByIdで検索
            Optional<Report> option = reportRepository.findById(employeeCode);
            // 取得できなかった場合はnullを返す
            Report report = option.orElse(null);
            return report;
        }

        /** 日報の登録を行なう */
        @Transactional
        public Report saveReport(Report report) {
            return reportRepository.save(report);
        }

}