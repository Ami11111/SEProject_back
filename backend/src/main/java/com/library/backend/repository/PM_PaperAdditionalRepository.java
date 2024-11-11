package com.library.backend.repository;

import com.library.backend.entity.PM_PaperAdditional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface PM_PaperAdditionalRepository extends JpaRepository<PM_PaperAdditional, Long> {

    @Transactional
    @Modifying
    void deleteAllByDoi(String doi);
}
