package com.library.backend.repository;

import com.library.backend.entity.PM_User;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface PM_UserRepository extends JpaRepository<PM_User, String> {

    PM_User findByUsername(String name);

    @Transactional
    void deleteById(int id);

    List<PM_User> findAllByUsernameContaining(String name);
}
