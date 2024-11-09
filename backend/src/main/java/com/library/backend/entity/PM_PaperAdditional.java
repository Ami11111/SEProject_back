package com.library.backend.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@Table(name = "paper_additional")
@IdClass(PM_PaperAdditional.class)
public class PM_PaperAdditional implements Serializable {
    @Id
    private String doi;
    @Id
    @Enumerated(EnumType.STRING)
    private Key key;
    private String value;

    public enum Key {
        correspondingAuthor, pageCount, conferenceOrPeriodical, acronym,
        publisher, fund, submitTime, receiptTime, publishTime, type
    }
}