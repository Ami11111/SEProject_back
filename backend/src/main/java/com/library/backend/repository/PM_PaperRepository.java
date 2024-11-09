package com.library.backend.repository;

import com.library.backend.entity.PM_Paper;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PM_PaperRepository extends JpaRepository<PM_Paper, String> {

    List<PM_Paper> findAll();

    List<PM_Paper> findByDoi(String doi);

    List<PM_Paper> findByAuthorListContaining(String author); 
} 