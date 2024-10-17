package com.library.backend.repository;

import com.library.backend.entity.Manager;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManagerRepository extends JpaRepository<Manager, String> {

    Manager findByUsername(String name);
}
