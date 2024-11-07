package com.library.backend.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "admin")
public class PM_Admin {
    @Id
    private int id;
    private String password;
}
