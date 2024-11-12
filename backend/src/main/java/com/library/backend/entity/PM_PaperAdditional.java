package com.library.backend.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
@Entity
@Table(name = "paper_additional")
@IdClass(PM_PaperAdditional.class)
public class PM_PaperAdditional implements Serializable {
    @Id
    private String doi;
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "`key`")
    private Key key;
    private String value;

    public enum Key {
        correspondingAuthor, pageCount, conferenceOrPeriodical, acronym,
        publisher, fund, submitTime, receiptTime, publishTime, type
    }

    // 转换方法，不包含 doi
    public Map<String, String> toMapWithoutDoi() {
        Map<String, String> map = new HashMap<>();
        map.put("key", String.valueOf(this.key));
        map.put("value", this.value);
        return map;
    }
}