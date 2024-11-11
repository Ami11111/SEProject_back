package com.library.backend.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "user_paper_claim")
@IdClass(PM_UserPaperClaim.class)
public class PM_UserPaperClaim implements Serializable {
    @Id
    @Column(name = "author_id")
    private int authorId;

    @Id
    @Column(name = "paper_doi")
    private String paperDoi;
}