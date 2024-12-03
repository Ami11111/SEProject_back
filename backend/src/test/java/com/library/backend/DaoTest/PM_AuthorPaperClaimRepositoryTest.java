package com.library.backend.DaoTest;

import com.library.backend.entity.PM_AuthorPaperClaim;
import com.library.backend.entity.PM_Paper;
import com.library.backend.entity.PM_User;
import com.library.backend.repository.PM_AuthorPaperClaimRepository;
import com.library.backend.repository.PM_PaperRepository;
import com.library.backend.repository.PM_UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PM_AuthorPaperClaimRepositoryTest {


    @Autowired
    private PM_AuthorPaperClaimRepository authorPaperClaimRepository;

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
    void findByAuthorIdAndPaperDoi() {
        PM_AuthorPaperClaim claim = new PM_AuthorPaperClaim();
        claim.setAuthorId(12345);
        claim.setPaperDoi("10.1234/test");
        authorPaperClaimRepository.save(claim);
        PM_AuthorPaperClaim foundClaim = authorPaperClaimRepository.findByAuthorIdAndPaperDoi(12345, "10.1234/test");
        assertNotNull(foundClaim);
        assertEquals(12345, foundClaim.getAuthorId());
        assertEquals("10.1234/test", foundClaim.getPaperDoi());
    }

    @Test
    @Transactional
    void deleteByAuthorIdAndPaperDoi() {
        PM_AuthorPaperClaim claim = new PM_AuthorPaperClaim();
        claim.setAuthorId(12345);
        claim.setPaperDoi("10.1234/test");
        authorPaperClaimRepository.save(claim);
        authorPaperClaimRepository.deleteByAuthorIdAndPaperDoi(12345, "10.1234/test");
        PM_AuthorPaperClaim foundClaim = authorPaperClaimRepository.findByAuthorIdAndPaperDoi(12345, "10.1234/test");
        assertNull(foundClaim);
    }
}