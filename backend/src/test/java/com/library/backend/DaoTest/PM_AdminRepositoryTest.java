package com.library.backend.DaoTest;

import com.library.backend.entity.PM_Admin;
import com.library.backend.repository.PM_AdminRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PM_AdminRepositoryTest {

    @Autowired
    private PM_AdminRepository adminRepository;

    @Test
    void findById() {
        PM_Admin newAdmin = new PM_Admin();
        newAdmin.setId(12345);
        newAdmin.setPassword("12345");
        adminRepository.save(newAdmin);

        PM_Admin foundAdmin = adminRepository.findById(12345);
        assertNotNull(foundAdmin);
        assertEquals(12345, foundAdmin.getId());
        assertEquals("12345", foundAdmin.getPassword());
    }

    @Test
    void countByIdAndPassword() {
        PM_Admin newAdmin = new PM_Admin();
        newAdmin.setId(12345);
        newAdmin.setPassword("12345");
        adminRepository.save(newAdmin);

        int count = adminRepository.countByIdAndPassword(12345, "12345");
        assertEquals(1, count);

        adminRepository.deleteById((long) 12345);
        count = adminRepository.countByIdAndPassword(12345, "12345");
        assertEquals(0, count);
    }
}