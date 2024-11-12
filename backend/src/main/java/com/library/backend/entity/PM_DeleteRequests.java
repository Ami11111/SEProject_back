package com.library.backend.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "delete_requests")
public class PM_DeleteRequests {
//    自增主键 requestId
    @Id
    private int requestId;
//    申请人 userId
    private int userId;
//    论文 doi
    private String doi;
}
