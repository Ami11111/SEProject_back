package com.library.backend.repository;

import com.library.backend.entity.Book;
import com.library.backend.entity.Borrow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface BorrowRepository extends JpaRepository<Borrow, String> {

    @Transactional
    void deleteById(int id);
    List<Borrow> findAllByBorrowNoContaining(String no);
    List<Borrow> findAllByStatusEqualsAndBorrowNoContaining(String status,String no);

    @Transactional
    @Modifying
    @Query(value = " update borrow set status = ?1 where id = ?2", nativeQuery = true)
    void updateInfo(String status, int id);

}
