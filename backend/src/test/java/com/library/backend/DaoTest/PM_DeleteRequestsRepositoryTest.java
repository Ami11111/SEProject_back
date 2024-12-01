package com.library.backend.DaoTest;

import com.library.backend.entity.PM_DeleteRequests;
import com.library.backend.repository.PM_DeleteRequestsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PM_DeleteRequestsRepositoryTest {

    @Autowired
    private PM_DeleteRequestsRepository deleteRequestsRepository;

    @Test
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
    void existsByDoiAndUserId() {
        PM_DeleteRequests request = new PM_DeleteRequests();
        request.setDoi("10.1234/test");
        request.setUserId(12345);
        deleteRequestsRepository.save(request);

        boolean exists = deleteRequestsRepository.existsByDoiAndUserId("10.1234/test", 12345);
        assertTrue(exists);
    }
}