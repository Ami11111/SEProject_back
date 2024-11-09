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
    @Column(name = "author_list")
    private String authorList;
    @Column(name = "first_author")
    private String firstAuthor;

    @Enumerated(EnumType.STRING) 
    private CCF ccf; 

    @Lob
    @Column(name = "file_data")
    private byte[] fileData; 

    @Enumerated(EnumType.STRING)
    private Status status;

    private String recommend;

    public enum CCF {
        A, B, C
    }

    public enum Status {
        notSubmit, review, approve, reject
    }
}