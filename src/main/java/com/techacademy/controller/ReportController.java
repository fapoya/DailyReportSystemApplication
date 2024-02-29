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

        // 日報詳細画面
        @GetMapping(value = "/{id}/")
        public String detail(@PathVariable String id, Model model) {

            model.addAttribute("report", reportService.findByCode(id));
            return "reports/detail";
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
                String employeeCode = employee.getCode(); // これは例です。実際のメソッド名や取得方法は実装に依存します。

                // Reportオブジェクトにemployee_codeを設定
                report.setEmployeeCode(employeeCode);

            return "reports/new";
        }

        // 日報新規登録処理
        @PostMapping(value = "/add")
        public String add(@Validated Report report, BindingResult res, Model model,@AuthenticationPrincipal UserDetail userDetails) {

            // 入力チェック
            if (res.hasErrors()) {
                return create(report, model, userDetails);
            }
            // 論理削除を行った従業員番号を指定すると例外となるためtry~catchで対応
            // (findByIdでは削除フラグがTRUEのデータが取得出来ないため)
            try {
                ErrorKinds result = reportService.save(report);

                if (ErrorMessage.contains(result)) {
                    model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
                    return create(report, model, userDetails);
                }

            } catch (DataIntegrityViolationException e) {
                model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DUPLICATE_EXCEPTION_ERROR),
                        ErrorMessage.getErrorValue(ErrorKinds.DUPLICATE_EXCEPTION_ERROR));
                return create(report, model, userDetails);
            }

         // Report登録

            return "redirect:/reports";
        }

        //日報更新
        @GetMapping("/{id}/update")
        public String edit(@PathVariable String id, Model model, Report report) {

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
        public String update(@Validated Report report, BindingResult res, @PathVariable String id, Model model) {

            if (res.hasErrors()) {
                // バリデーションエラーがある場合
                model.addAttribute("report", report);
                return edit(null, model, report);
            }
            // 日報の更新処理
            reportService.update(id, report);
            return "redirect:/reports";
        }
}
