package com.techacademy.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.SQLRestriction;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "reports")
@SQLRestriction("delete_flg = false")
public class Report {

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "employee_code", referencedColumnName = "code", nullable = false)
    private Employee employee;

    //データベースがIDを自動生成
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //日報日付
    @NotNull(message="値を入力してください")
    @Column(nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate reportDate;

    //タイトル
    @Column(length = 100, nullable = false)
    @NotBlank(message="値を入力してください")
    @Length(max = 100)
    private String title;

    //内容
    @Column(columnDefinition="LONGTEXT",length = 600, nullable = false)
    @NotBlank(message="値を入力してください")
    @Length(max = 600)
    private String content;

    // 削除フラグ(論理削除を行うため)
    @Column(columnDefinition="TINYINT", nullable = false)
    private boolean deleteFlg;

    // 登録日時
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 更新日時
    private LocalDateTime updatedAt;

    public void setEmployeeCode(String employeeCode) {
        // TODO 自動生成されたメソッド・スタブ

    }

}
