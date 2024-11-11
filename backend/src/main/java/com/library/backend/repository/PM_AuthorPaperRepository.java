package com.library.backend.repository;

import com.library.backend.entity.PM_AuthorPaper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface PM_AuthorPaperRepository extends JpaRepository<PM_AuthorPaper, Long> {

    @Transactional
    @Modifying
    void deleteAllByPaperId(String paperId);

}
