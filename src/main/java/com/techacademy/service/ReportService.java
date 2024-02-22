package com.techacademy.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.constants.ErrorMessage;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.repository.EmployeeRepository;
import com.techacademy.repository.ReportRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ReportService(ReportRepository reportRepository, PasswordEncoder passwordEncoder) {
        this.reportRepository = reportRepository;
            this.passwordEncoder = passwordEncoder;
    }

        // 日報保存
        @Transactional
        public ErrorKinds save(Report report) {

            // 記載なしチェック

            report.setDeleteFlg(false);

            LocalDateTime now = LocalDateTime.now();
            report.setCreatedAt(now);
            report.setUpdatedAt(now);

            reportRepository.save(report);
            return ErrorKinds.SUCCESS;
        }


        // 更新（追加）を行なう

        @Transactional
        public ErrorKinds update(String code, Report updatedReport) {
            Optional<Report> existingReportOpt = reportRepository.findById(code);
            if (!existingReportOpt.isPresent()) {
                return ErrorKinds.BLANK_ERROR; // 従業員が見つからない場合のエラー処理
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


        // 従業員削除
        @Transactional
        public ErrorKinds delete(String code, UserDetail userDetail) {

            // 自分を削除しようとした場合はエラーメッセージを表示
            if (code.equals(userDetail.getEmployee().getCode())) {
                return ErrorKinds.LOGINCHECK_ERROR;
            }
            Report report = findByCode(code);
            LocalDateTime now = LocalDateTime.now();
            report.setUpdatedAt(now);
            report.setDeleteFlg(true);

            return ErrorKinds.SUCCESS;
        }

        // 従業員一覧表示処理
        public List<Report> findAll() {
            return reportRepository.findAll();
        }

        // 1件を検索
        public Report findByCode(String code) {
            // findByIdで検索
            Optional<Report> option = reportRepository.findById(code);
            // 取得できなかった場合はnullを返す
            Report report = option.orElse(null);
            return report;
        }

        // 従業員パスワードチェック
        public ErrorKinds employeePasswordCheck(Employee employee) {

            // 従業員パスワードの半角英数字チェック処理
            if (isHalfSizeCheckError(employee)) {

                return ErrorKinds.HALFSIZE_ERROR;
            }

            // 従業員パスワードの8文字～16文字チェック処理
            if (isOutOfRangePassword(employee)) {

                return ErrorKinds.RANGECHECK_ERROR;
            }

            employee.setPassword(passwordEncoder.encode(employee.getPassword()));

            return ErrorKinds.CHECK_OK;
        }

        // 従業員パスワードの半角英数字チェック処理
        private boolean isHalfSizeCheckError(Employee employee) {

            // 半角英数字チェック
            Pattern pattern = Pattern.compile("^[A-Za-z0-9]+$");
            Matcher matcher = pattern.matcher(employee.getPassword());
            return !matcher.matches();
        }

        // 従業員パスワードの8文字～16文字チェック処理
        public boolean isOutOfRangePassword(Employee employee) {

            // 桁数チェック
            int passwordLength = employee.getPassword().length();
            return passwordLength < 8 || 16 < passwordLength;
        }

    }
