package com.techacademy.controller;

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

        // 従業員詳細画面
        @GetMapping(value = "/{employeeCode}/")
        public String detail(@PathVariable String employeeCode, Model model) {

            model.addAttribute("report", reportService.findByCode(employeeCode));
            return "reports/detail";
        }

        // 従業員新規登録画面
        @GetMapping(value = "/add")
        public String create(@ModelAttribute Report report, Model model, @AuthenticationPrincipal UserDetail userDetails) {
            if (userDetails != null) {
                Employee employee = userDetails.getEmployee();
                // Employee情報をModelに追加
                model.addAttribute("name", employee.getName());
            } else {
                // エラーハンドリング: 認証されていない場合や、期待したUserDetailが取得できなかった場合の処理
                return "redirect:/reports";
            }
            return "reports/new";
        }

        // 従業員新規登録処理
        @PostMapping(value = "/add")
        public String add(@Validated Report report, BindingResult res, Model model) {

            // 空白チェック
            /*
             * エンティティ側の入力チェックでも実装は行えるが、更新の方でパスワードが空白でもチェックエラーを出さずに
             * 更新出来る仕様となっているため上記を考慮した場合に別でエラーメッセージを出す方法が簡単だと判断
             */
            if ("".equals(report.getReportDate())) {
                // 日付が空白だった場合
                model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.BLANK_ERROR),
                        ErrorMessage.getErrorValue(ErrorKinds.BLANK_ERROR));
                return create(report, model, null);
            }
            if ("".equals(report.getTitle())) {
                // タイトルが空白だった場合
                model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.BLANK_ERROR),
                        ErrorMessage.getErrorValue(ErrorKinds.BLANK_ERROR));
                return create(report, model, null);
            }
            if ("".equals(report.getContent())) {
                // 内容が空白だった場合
                model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.BLANK_ERROR),
                        ErrorMessage.getErrorValue(ErrorKinds.BLANK_ERROR));
                return create(report, model, null);
            }
            // 入力チェック
            if (res.hasErrors()) {
                return create(report, model, null);
            }

            // 論理削除を行った従業員番号を指定すると例外となるためtry~catchで対応
            // (findByIdでは削除フラグがTRUEのデータが取得出来ないため)
            try {
                ErrorKinds result = reportService.save(report);

                if (ErrorMessage.contains(result)) {
                    model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
                    return create(report, model, null);
                }

            } catch (DataIntegrityViolationException e) {
                model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DATECHECK_ERROR),
                        ErrorMessage.getErrorValue(ErrorKinds.DATECHECK_ERROR));
                return create(report, model, null);
            }
            return "redirect:/reports";
        }
}
