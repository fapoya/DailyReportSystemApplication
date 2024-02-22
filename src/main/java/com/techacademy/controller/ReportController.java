    package com.techacademy.controller;

    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.dao.DataIntegrityViolationException;
    import org.springframework.security.core.annotation.AuthenticationPrincipal;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.validation.BindingResult;
    import org.springframework.validation.annotation.Validated;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.ModelAttribute;
    import org.springframework.web.bind.annotation.PathVariable;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.RequestMapping;

    import com.techacademy.constants.ErrorKinds;
    import com.techacademy.constants.ErrorMessage;

    import com.techacademy.entity.Employee;
    import com.techacademy.service.EmployeeService;
    import com.techacademy.service.UserDetail;

    @Controller
    @RequestMapping("reports")
    public class ReportController {


            // 日報一覧画面
            @GetMapping
            public String list(Model model) {


                return "reports/list";
            }

            // 日報詳細画面
            @GetMapping(value = "/{code}/")
            public String detail(@PathVariable String code, Model model) {

                return "reports/detail";
            }

            // 日報新規登録画面
            @GetMapping(value = "/add")
            public String create(@ModelAttribute Employee employee) {

                return "reports/new";
            }

            //日報　更新------------------------------------
            @GetMapping(value = "/{code}/update")
            public String edit(@PathVariable(name = "code", required = false) String code, Model model, Employee employee) {
                return "reports/update";
            }
    }
