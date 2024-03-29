package com.techacademy.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.constants.ErrorMessage;

import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;
import com.techacademy.repository.ReportRepository;
import com.techacademy.service.EmployeeService;
import com.techacademy.service.ReportService;
import com.techacademy.service.UserDetail;

@Controller
@RequestMapping("reports")
public class ReportController {
    private final ReportService reportService;

        @Autowired
        public ReportController(ReportService reportService) {
            this.reportService = reportService;
        }

        // 日報一覧画面
        @GetMapping
        public String list(@AuthenticationPrincipal UserDetail userDetail, Model model) {
            // 現在のユーザーの権限を取得
            String role = userDetail.getAuthorities().stream()
                            .findFirst()
                            .map(GrantedAuthority::getAuthority)
                            .orElse("USER"); // デフォルトを一般ユーザーとする

            // 権限に基づいて日報のリストを取得
            List<Report> reports = reportService.findReportsByUserIdAndRole(userDetail.getEmployee().getCode(), role);

            // モデルに属性を追加
            model.addAttribute("reportList", reports);
            model.addAttribute("listSize", reports.size());
            return "reports/list";
        }

        // 日報詳細画面
        @GetMapping(value = "/{id}/")
        public String detail(@PathVariable Long id, Model model
                , @AuthenticationPrincipal UserDetail userDetails) {

            Report report = reportService.findByCode(id);

            //ログインユーザーの権限を取得
            String role = userDetails.getAuthorities().stream()
                            .findFirst()
                            .map(GrantedAuthority::getAuthority)
                            .orElse("USER");

            /*
             * 権限が”ADMIN”の場合、または報告書の従業員コードとログインユーザーの
             * 従業員コードが一致する場合に詳細を表示
             */
            if("ADMIN".equals(role) || report.getEmployee().getCode()
                      .equals(userDetails.getEmployee().getCode())) {
                model.addAttribute("report", report);
                return "reports/detail";
            }else {
                return "redirect:/reports";
            }
        }

        // 日報新規登録画面
        @GetMapping(value = "/add")
        public String create(@ModelAttribute Report report, Model model, @AuthenticationPrincipal UserDetail userDetails) {

                model.addAttribute(userDetails.getEmployee());
                // 従業員オブジェクトを取得
                Employee employee = userDetails.getEmployee();
                // 従業員の名前をモデルに追加
                model.addAttribute("name", employee.getName());
             // UserDetailからemployee_codeを取得
                String employeeCode = employee.getCode();

                // Reportオブジェクトにemployee_codeを設定
                report.setEmployee(employee);

            return "reports/new";
        }
        // 日報新規登録処理
        @PostMapping(value = "/add")
        public String add(@Validated Report report, BindingResult res, Model model,@AuthenticationPrincipal UserDetail userDetails) {

            model.addAttribute(userDetails.getEmployee());
            // 従業員オブジェクトを取得
            Employee employee2 = userDetails.getEmployee();
            // 従業員の名前をモデルに追加
            model.addAttribute("name", employee2.getName());

            // 入力チェック
            if (res.hasErrors()) {
                model.addAttribute("report", report);
                return "reports/new";
            }
            // ログインしているユーザーのEmployeeオブジェクトをReportに設定
            Employee employee = userDetails.getEmployee(); // ログインユーザーの従業員情報を取得
            if (employee != null) {
                report.setEmployee(employee); // ReportオブジェクトにEmployeeを設定
            } else {
                // Employee情報が取得できない場合のエラーハンドリング
                model.addAttribute("errorMessage", "Employee情報が取得できません。");
                return "reports/new";
            }

            // 日付が既に登録されているか検証
            if (reportService.isReportDateAlreadyExists(employee.getCode(), report.getReportDate())) {
                model.addAttribute("reportDateError", "既に登録されている日付です。");
                return "reports/new";
            }


         // Report登録
            reportService.saveReport(report);
            return "redirect:/reports";
        }

        //日報更新
        @GetMapping("/{id}/update")
        public String edit(@PathVariable Long id, Model model, Report report, @AuthenticationPrincipal UserDetail userDetails) {

            model.addAttribute(userDetails.getEmployee());
            // 従業員オブジェクトを取得
            Employee employee = userDetails.getEmployee();
            // 従業員の名前をモデルに追加
            model.addAttribute("name", employee.getName());

            if(id != null) {
                model.addAttribute("report", reportService.findByCode(id));
            }else {
                // Modelに登録

            model.addAttribute("report", report);
            }

        return "reports/update";
        }

        //日報更新処理
        @PostMapping("/{id}/update")
        public String update(@Validated Report report, BindingResult res, @PathVariable Long id, Model model, @AuthenticationPrincipal UserDetail userDetails) {

            model.addAttribute(userDetails.getEmployee());
            // 従業員オブジェクトを取得
            Employee employee2 = userDetails.getEmployee();
            // 従業員の名前をモデルに追加
            model.addAttribute("name", employee2.getName());

            // 入力チェック
            if (res.hasErrors()) {
                model.addAttribute("report", report);
                return "reports/update";
            }
            // ログインしているユーザーのEmployeeオブジェクトをReportに設定
            Employee employee = userDetails.getEmployee(); // ログインユーザーの従業員情報を取得
            if (employee != null) {
                report.setEmployee(employee); // ReportオブジェクトにEmployeeを設定
            } else {
                // Employee情報が取得できない場合のエラーハンドリング
                model.addAttribute("errorMessage", "Employee情報が取得できません。");
                return "reports/update";
            }

         // 日付が既に登録されているか検証（更新時）
            if (reportService.isReportDateAlreadyExistsForUpdate(employee.getCode(), report.getReportDate(), report.getId())) {
                model.addAttribute("reportDateError", "既に登録されている日付です。");
                return "reports/update";
            }

            // 日報の更新処理
            reportService.update(id, report);
            return "redirect:/reports";
        }

        // 従業員削除処理
        @PostMapping(value = "/{id}/delete")
        public String delete(@PathVariable Long id, @AuthenticationPrincipal UserDetail userDetails, Model model) {

            ErrorKinds result = reportService.delete(id, userDetails);

            if (ErrorMessage.contains(result)) {
                model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
                model.addAttribute("report", reportService.findByCode(id));
                return detail(id, model, userDetails);
            }

            return "redirect:/reports";
        }
}