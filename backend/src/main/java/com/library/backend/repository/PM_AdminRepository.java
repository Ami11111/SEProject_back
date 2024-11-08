package com.library.backend.repository;

import com.library.backend.entity.PM_Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface PM_AdminRepository extends JpaRepository<PM_Admin, Long> {
    PM_Admin findById(int id);

    int countByIdAndPassword(int id, String password);
}
