package com.library.backend.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "author_paper")
@IdClass(PM_AuthorPaper.class)
public class PM_AuthorPaper implements Serializable {
    @Id
    @Column(name = "a_id")
    private int authorId;
    @Id
    @Column(name = "p_id")
    private String paperId;
    @Enumerated(EnumType.STRING) 
    private Seq seq;

    public enum Seq {
        first,second,third
    }
}