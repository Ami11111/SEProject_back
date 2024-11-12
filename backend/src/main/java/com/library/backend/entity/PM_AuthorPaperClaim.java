package com.library.backend.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "author_paper_claim")
@IdClass(PM_AuthorPaperClaim.class)
public class PM_AuthorPaperClaim implements Serializable {
    @Id
    @Column(name = "author_id")
    public int authorId;

    @Id
    @Column(name = "paper_doi")
    public String paperDoi;
}