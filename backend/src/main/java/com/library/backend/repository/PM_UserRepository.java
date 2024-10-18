package com.library.backend.repository;

import com.library.backend.entity.PM_User;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface PM_UserRepository extends JpaRepository<PM_User, String> {

    int countByNameAndPassword(String name, String password);

    PM_User findByName(String name);

    @Transactional
    void deleteById(int id);

    List<PM_User> findAllByNameContaining(String name);
}
