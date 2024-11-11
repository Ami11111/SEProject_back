package com.library.backend.repository;

import com.library.backend.entity.PM_Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PM_AdminRepository extends JpaRepository<PM_Admin, Long> {
    PM_Admin findById(int id);

    int countByIdAndPassword(int id, String password);
}
