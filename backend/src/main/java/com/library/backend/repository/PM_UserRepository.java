package com.library.backend.repository;

import com.library.backend.entity.PM_User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PM_UserRepository extends JpaRepository<PM_User, Long> {

    int countByIdAndPassword(int id, String password);
    int countById(int id);

    PM_User findByName(String name);
    PM_User findById(int id);

    @Transactional
    @Modifying
    void deleteById(int id);
    @Transactional
    @Modifying
    @Query(value = "update PM_User u set u.name = :name, u.phone = :phone, u.email = :email, u.address = :address where u.id = :id")
    void updateUserInfoById(@Param("id") int id,
                            @Param("name") String name,
                            @Param("phone") String phone,
                            @Param("email") String email,
                            @Param("address") String address);

    List<PM_User> findAllByNameContaining(String name);

    List<PM_User> findAll();

    @Transactional
    @Modifying
    @Query(value = "update PM_User u set u.password =:password where u.id =:id")
    void resetPasswordById(@Param("id") int id, @Param("password") String password);
}
