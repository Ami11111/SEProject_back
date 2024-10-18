package com.library.backend.repository;

import com.library.backend.entity.PM_User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import javax.transaction.Transactional;
import java.util.List;

public interface PM_UserRepository extends JpaRepository<PM_User, Long> {

    int countByUsernameAndPassword(String name, String password);

    PM_User findByUsername(String name);

    @Transactional
    @Modifying
    void deleteById(int id);
    @Transactional
    @Modifying
    void updateUsernameById(String usrname, int id);

    List<PM_User> findAllByUsernameContaining(String name);
}
