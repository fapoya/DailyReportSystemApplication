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
import com.techacademy.security.CustomUserDetails;
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
        public String showReports(@RequestParam String employeeCode, @RequestParam String role, Model model) {
            List<Report> reports = reportService.findReportsByUserIdAndRole(employeeCode, role);


            model.addAttribute("reports", reports);
            return "reports/list";
        }

        // 日報詳細画面
        @GetMapping(value = "/{findByEmployee_Code}/")
        public String detail(@PathVariable String findByEmployee_Code, Model model) {

            return "reports/detail";
        }

        // 日報新規登録画面
        @GetMapping(value = "/add")
        public String create(@ModelAttribute Employee employee) {

            return "reports/new";
        }

        //日報　更新------------------------------------
        @GetMapping(value = "/{findByEmployee_Code}/update")
        public String edit(@PathVariable(name = "findByEmployee_Code", required = false) String code, Model model, Employee employee) {
            return "reports/update";
        }
}