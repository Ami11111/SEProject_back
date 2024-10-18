package com.library.backend.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "user")
public class PM_User {
    @Id
    private Long id;
    private String name;
    private String password;
    private String email;
    private String phone;
    private String address;
    private boolean role;
}
