package com.library.backend.repository;

import com.library.backend.entity.PM_User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

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
    @Query(value = "update PM_User u set u.username = ?2 where u.id = ?1")
    void updateUsernameById(int id, String username);

    List<PM_User> findAllByUsernameContaining(String name);
}
