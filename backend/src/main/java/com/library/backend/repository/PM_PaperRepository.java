package com.library.backend.repository;

import com.library.backend.entity.PM_Paper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface PM_PaperRepository extends JpaRepository<PM_Paper, String> {
    // 根据 doi 更新 paper 的 fileData
    @Transactional // 保证事务
    @Modifying
    @Query("UPDATE PM_Paper p SET p.fileData = :fileData WHERE p.doi = :doi")
    void updateFileDataByDoi(@Param("fileData") byte[] fileData, @Param("doi") String doi);

    int countByDoi(String doi);

    List<PM_Paper> findAll();

    List<PM_Paper> findByDoi(String doi);

    List<PM_Paper> findByAuthorListContaining(String author);
}