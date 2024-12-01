package com.library.backend.DaoTest;

import com.library.backend.entity.PM_AuthorPaperClaim;
import com.library.backend.repository.PM_AuthorPaperClaimRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PM_AuthorPaperClaimRepositoryTest {

    @Autowired
    private PM_AuthorPaperClaimRepository authorPaperClaimRepository;

    @Test
    void deleteByAuthorIdAndPaperDoi() {
        PM_AuthorPaperClaim claim = new PM_AuthorPaperClaim();
        claim.setAuthorId(12345);
        claim.setPaperDoi("10.1234/test");
        authorPaperClaimRepository.save(claim);

        authorPaperClaimRepository.deleteByAuthorIdAndPaperDoi(12345, "10.1234/test");
        PM_AuthorPaperClaim foundClaim = authorPaperClaimRepository.findByAuthorIdAndPaperDoi(12345, "10.1234/test");
        assertNull(foundClaim);
    }

    @Test
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
}