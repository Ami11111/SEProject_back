package com.library.backend.repository;

import com.library.backend.entity.Manager;
import com.library.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, String> {

    User findByUsername(String name);

    @Transactional
    void deleteById(int id);

    List<User> findAllByUsernameContaining(String name);
}
