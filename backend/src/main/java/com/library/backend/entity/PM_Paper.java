package com.library.backend.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Lob;
import javax.persistence.Enumerated;
import javax.persistence.EnumType;

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
    private CCF ccf; 

    @Lob 
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