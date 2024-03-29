package com.techacademy.controller;

import java.util.List;

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
import org.springframework.web.bind.annotation.RequestParam;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.constants.ErrorMessage;

import com.techacademy.entity.Employee;
import com.techacademy.service.EmployeeService;
import com.techacademy.service.UserDetail;

@Controller
@RequestMapping("employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // 従業員一覧画面
    @GetMapping
    public String list(Model model, @RequestParam(name = "name", required = false)String name) {

        List<Employee> employees;
        if(name == null || name.isEmpty()) {
            employees = employeeService.findAll();
        }else {
            employees = employeeService.findByNameLike(name);
        }

        model.addAttribute("listSize", employees.size());
        model.addAttribute("employeeList", employees);
        model.addAttribute("name", name);

        return "employees/list";
    }

    // 従業員詳細画面
    @GetMapping(value = "/{code}/")
    public String detail(@PathVariable String code, Model model) {

        model.addAttribute("employee", employeeService.findByCode(code));
        return "employees/detail";
    }

    // 従業員新規登録画面
    @GetMapping(value = "/add")
    public String create(@ModelAttribute Employee employee) {

        return "employees/new";
    }

    // 従業員新規登録処理
    @PostMapping(value = "/add")
    public String add(@Validated Employee employee, BindingResult res, Model model) {

        // パスワード空白チェック
        /*
         * エンティティ側の入力チェックでも実装は行えるが、更新の方でパスワードが空白で
         *もチェックエラーを出さずに
         * 更新出来る仕様となっているため上記を考慮した場合に別でエラーメッセージを出
         *す方法が簡単だと判断
         */
        if ("".equals(employee.getPassword())) {
            // パスワードが空白だった場合
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.BLANK_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.BLANK_ERROR));

            return create(employee);

        }

        // 入力チェック
        if (res.hasErrors()) {
            return create(employee);
        }

        // 論理削除を行った従業員番号を指定すると例外となるためtry~catchで対応
        // (findByIdでは削除フラグがTRUEのデータが取得出来ないため)
        try {
            ErrorKinds result = employeeService.save(employee);

            if (ErrorMessage.contains(result)) {
                model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
                return create(employee);
            }

        } catch (DataIntegrityViolationException e) {
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DUPLICATE_EXCEPTION_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.DUPLICATE_EXCEPTION_ERROR));
            return create(employee);
        }

        return "redirect:/employees";
    }


    //従業員　更新-------------------------------------
    @GetMapping(value = "/{code}/update")
    public String edit(@PathVariable(name = "code", required = false) String code, Model model, Employee employee) {
        if(code != null) {
            model.addAttribute("employee", employeeService.findByCode(code));
        }else {
        // Modelに登録
            model.addAttribute("employee", employee);
        }
        return "employees/update";
    }

    @PostMapping("/{code}/update")
    public String update(@Validated Employee employee, BindingResult res, @PathVariable String code, Model model) {
        // パスワードが空でない場合にのみパスワード検証を実施
        // Javaの標準的な方法でパスワードが空でないかチェック
        if (employee.getPassword() != null && !employee.getPassword().isEmpty()) {
            ErrorKinds passwordCheckResult = employeeService.employeePasswordCheck(employee);
            if (passwordCheckResult != ErrorKinds.CHECK_OK) {
                // パスワード検証エラーの処理
                model.addAttribute(ErrorMessage.getErrorName(passwordCheckResult),
                                    ErrorMessage.getErrorValue(passwordCheckResult));
                return edit(code, model, employee);
            }
        }

        if (res.hasErrors()) {
            // その他のバリデーションエラーがある場合
            model.addAttribute("employee", employee);
            return edit(null, model, employee);
        }
        // 従業員情報の更新処理
        employeeService.update(code, employee);
        return "redirect:/employees";
    }

    // 従業員削除処理
    @PostMapping(value = "/{code}/delete")
    public String delete(@PathVariable String code, @AuthenticationPrincipal UserDetail userDetail, Model model) {

        ErrorKinds result = employeeService.delete(code, userDetail, null);

        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            model.addAttribute("employee", employeeService.findByCode(code));
            return detail(code, model);
        }

        return "redirect:/employees";
    }

}