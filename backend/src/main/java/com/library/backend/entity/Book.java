package com.library.backend.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "book")
public class Book {

    @Id
    private int id;
    private String bookName;
    private String author;
    private String press;
    private int borrow;
}
