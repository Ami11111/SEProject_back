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

    private String authorList;

    private String firstAuthor;

    @Enumerated(EnumType.STRING)
    private Ccf ccf;
    public enum Ccf {
        A,
        B,
        C
    }

    @Enumerated(EnumType.STRING)
    private Status status;
    public enum Status {
        NO_SUBMIT,
        REVIEW,
        REJECT,
        APPROVE
    }
    @Lob
    @Column(columnDefinition = "MEDIUMBLOB")
    private byte[] fileData;

    @Lob  // 可选注解
    @Column(columnDefinition = "TEXT")
    private String recommend;

}
