package com.library.backend.repository;

import com.library.backend.entity.PM_DeleteRequests;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface PM_DeleteRequestsRepository extends JpaRepository<PM_DeleteRequests, Long> {
    List<PM_DeleteRequests> findAll ();

//    根据 doi 和 userId 删除记录
    @Transactional
    int deleteByDoiAndUserId(String doi, int userId);

    boolean existsByDoiAndUserId(String doi, int userId);
}
