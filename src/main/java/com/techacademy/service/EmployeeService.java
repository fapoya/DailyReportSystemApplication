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
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @Autowired
    private ReportService reportService;

    // 従業員保存
    @Transactional
    public ErrorKinds save(Employee employee) {

        // パスワードチェック
        ErrorKinds result = employeePasswordCheck(employee);
        if (ErrorKinds.CHECK_OK != result) {
            return result;
        }

        // 従業員番号重複チェック
        if (findByCode(employee.getCode()) != null) {
            return ErrorKinds.DUPLICATE_ERROR;
        }

        employee.setDeleteFlg(false);

        LocalDateTime now = LocalDateTime.now();
        employee.setCreatedAt(now);
        employee.setUpdatedAt(now);

        employeeRepository.save(employee);
        return ErrorKinds.SUCCESS;
    }


    // 更新（追加）を行なう

    @Transactional
    public ErrorKinds update(String code, Employee updatedEmployee) {
        Optional<Employee> existingEmployeeOpt = employeeRepository.findById(code);
        if (!existingEmployeeOpt.isPresent()) {
            return ErrorKinds.BLANK_ERROR; // 従業員が見つからない場合のエラー処理
        }
        Employee existingEmployee = existingEmployeeOpt.get();

        //パスワードが空でない場合にのみ、新しいパスワードを設定
        if(updatedEmployee.getPassword() == null || updatedEmployee.getPassword().isEmpty()) {
           updatedEmployee.setPassword(existingEmployee.getPassword());
        }

        // 既存のcreated_atの値を新しいEmployeeオブジェクトに設定
        updatedEmployee.setCreatedAt(existingEmployee.getCreatedAt());

        // 更新処理でupdatedAtのみを現在時刻に設定
        updatedEmployee.setUpdatedAt(LocalDateTime.now());

        // リポジトリを使用してデータベースに保存
        employeeRepository.save(updatedEmployee);

        return ErrorKinds.SUCCESS;
    }


    // 従業員削除
    @Transactional
    public ErrorKinds delete(String code, UserDetail userDetail, Long id) {

        // 自分を削除しようとした場合はエラーメッセージを表示
        if (code.equals(userDetail.getEmployee().getCode())) {
            return ErrorKinds.LOGINCHECK_ERROR;
        }
        Employee employee = findByCode(code);
        LocalDateTime now = LocalDateTime.now();
        employee.setUpdatedAt(now);
        employee.setDeleteFlg(true);

        // 削除対象の従業員（employee）に紐づいている、日報のリスト（reportList）を取得
        List<Report> reportList = reportService.findByEmployee(employee);

        // 日報のリスト（reportList）を拡張for文を使って繰り返し
        for (Report report : reportList) {
            // 日報（report）のIDを指定して、日報情報を削除
            reportService.delete(report.getId(), userDetail);
        }

        return ErrorKinds.SUCCESS;
    }

    // 従業員一覧表示処理
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    // 1件を検索
    public Employee findByCode(String code) {
        // findByIdで検索
        Optional<Employee> option = employeeRepository.findById(code);
        // 取得できなかった場合はnullを返す
        Employee employee = option.orElse(null);
        return employee;
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

    public List<Employee> findByNameLike(String name) {
        return employeeRepository.findByNameLike("%" + name + "%");
    }

}