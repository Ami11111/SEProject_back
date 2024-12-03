package com.library.backend.DaoTest;

import com.library.backend.entity.PM_DeleteRequests;
import com.library.backend.entity.PM_Paper;
import com.library.backend.entity.PM_User;
import com.library.backend.repository.PM_DeleteRequestsRepository;
import com.library.backend.repository.PM_PaperRepository;
import com.library.backend.repository.PM_UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PM_DeleteRequestsRepositoryTest {

    @Autowired
    private PM_DeleteRequestsRepository deleteRequestsRepository;

    @Autowired
    private PM_UserRepository userRepository;
    @Autowired
    private PM_PaperRepository paperRepository;

    private PM_User testUser;
    private PM_Paper testPaper;

    @BeforeEach
    void setUp() {
        testUser = new PM_User();
        testUser.setId(12345);
        testUser.setName("Test User");
        testUser.setPassword("some_password");
        userRepository.save(testUser);
        testPaper = new PM_Paper();
        testPaper.setDoi("10.1234/test");
        testPaper.setTitle("Test Paper");
        testPaper.setStatus(PM_Paper.Status.notSubmit);
        paperRepository.save(testPaper);
    }
    @Test
    @Transactional
    void deleteByDoiAndUserId() {
        PM_DeleteRequests request = new PM_DeleteRequests();
        request.setDoi("10.1234/test");
        request.setUserId(12345);
        deleteRequestsRepository.save(request);

        int deletedCount = deleteRequestsRepository.deleteByDoiAndUserId("10.1234/test", 12345);
        assertEquals(1, deletedCount);

        boolean exists = deleteRequestsRepository.existsByDoiAndUserId("10.1234/test", 12345);
        assertFalse(exists);
    }

    @Test
    @Transactional
    void existsByDoiAndUserId() {
        PM_DeleteRequests request = new PM_DeleteRequests();
        request.setDoi("10.1234/test");
        request.setUserId(12345);
        deleteRequestsRepository.save(request);

        boolean exists = deleteRequestsRepository.existsByDoiAndUserId("10.1234/test", 12345);
        assertTrue(exists);
    }
}