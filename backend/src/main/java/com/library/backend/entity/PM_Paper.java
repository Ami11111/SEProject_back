package com.library.backend.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "paper")
public class PM_Paper {
    @Id
    private String doi;
    private String title;
    @Column(name = "first_author")
    private String firstAuthor;

    @Column(name = "second_author")
    private String secondAuthor;

    @Column(name = "third_author")
    private String thirdAuthor;

    @Enumerated(EnumType.STRING)
    private CCF ccf;

    @Lob
    @Column(name = "file_data")
    private byte[] fileData;

    private String url;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String recommend;

    public enum CCF {
        A, B, C
    }

    public enum Status {
        notSubmit, review, approve, reject
    }

    // 辅助方法：获取第一作者数组
    public String[] getFirstAuthorsArray() {
        return firstAuthor != null ? firstAuthor.split(",") : new String[0];
    }

    // 辅助方法：获取第二作者数组
    public String[] getSecondAuthorsArray() {
        return secondAuthor != null ? secondAuthor.split(",") : new String[0];
    }

    // 辅助方法：获取第三作者数组
    public String[] getThirdAuthorsArray() {
        return thirdAuthor != null ? thirdAuthor.split(",") : new String[0];
    }
}