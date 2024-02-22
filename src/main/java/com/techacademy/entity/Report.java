package com.techacademy.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.SQLRestriction;
import org.hibernate.validator.constraints.Length;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
@Entity
@Table(name = "reports")
@SQLRestriction("delete_flg = false")
public class Report {

    public static enum Role {
        GENERAL("一般"), ADMIN("管理者");

        private String name;

        private Role(String name) {
            this.name = name;
        }

        public String getValue() {
            return this.name;
        }
    }

    // ID
    @Id
    @Column(length = 10)
    @NotEmpty
    @Length(max = 10)
    private String code;

    // 削除フラグ(論理削除を行うため)
    @Column(columnDefinition="TINYINT", nullable = false)
    private boolean deleteFlg;

    // 登録日時
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 更新日時
    private LocalDateTime updatedAt;

    //日報作成日時
    @Column(nullable = false)
    private LocalDateTime report_date;

    //タイトル
    @Column(length = 20, nullable = false)
    @NotEmpty
    @Length(max = 20)
    private String title;

    //内容
    @Column(columnDefinition="LONGTEXT", nullable = false)
    @NotEmpty
    private String content;
}