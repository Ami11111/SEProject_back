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

    PM_Paper findByDoi(String doi);

    @Transactional
    @Modifying
    void deleteByDoi(String doi);

    @Transactional
    @Modifying
    @Query("UPDATE PM_Paper p SET p.url = :url WHERE p.doi = :doi")
    void updateUrlByDoi(@Param("url") String url, @Param("doi") String doi);

    @Query("SELECT p FROM PM_Paper p LEFT JOIN PM_AuthorPaper ap ON p.doi = ap.paperId " +
           "WHERE (:userId IS NULL OR ap.authorId = :userId) " +
           "AND (:doi IS NULL OR p.doi = :doi)")
    List<PM_Paper> findPapersByUserIdAndDoi(@Param("userId") Integer userId, @Param("doi") String doi);
}