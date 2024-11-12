package com.library.backend.repository;

import com.library.backend.entity.PM_AuthorPaperClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface PM_AuthorPaperClaimRepository extends JpaRepository<PM_AuthorPaperClaim, Long> {

    @Transactional
    @Modifying
    @Query("DELETE FROM PM_AuthorPaperClaim u WHERE u.authorId = :authorId AND u.paperDoi = :paperDoi")
    void deleteByAuthorIdAndPaperDoi(@Param("authorId") int authorId, @Param("paperDoi") String paperDoi);

    PM_AuthorPaperClaim findByAuthorIdAndPaperDoi(int authorId, String paperDoi);

    @Override
    <S extends PM_AuthorPaperClaim> S save(S userPaperClaim);
}