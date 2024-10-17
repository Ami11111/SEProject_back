package com.library.backend.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "manager")
public class Manager {

    @Id
    private int id;
    private String username;
    private String password;
}
