package com.library.backend.repository;

import com.library.backend.entity.Book;
import com.library.backend.entity.Manager;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface BookRepository extends JpaRepository<Book, String> {

    @Transactional
    void deleteById(int id);
    Book findById(int id);
    Book findByBookName(String name);
    List<Book> findAllByBookNameContaining(String name);
    List<Book> findAll();
}
