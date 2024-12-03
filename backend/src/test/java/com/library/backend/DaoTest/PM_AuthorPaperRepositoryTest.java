package com.library.backend.DaoTest;

import com.library.backend.entity.PM_AuthorPaper;
import com.library.backend.entity.PM_Paper;
import com.library.backend.entity.PM_User;
import com.library.backend.repository.PM_AuthorPaperRepository;
import com.library.backend.repository.PM_PaperRepository;
import com.library.backend.repository.PM_UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PM_AuthorPaperRepositoryTest {

    @Autowired
    private PM_AuthorPaperRepository authorPaperRepository;
    @Autowired
    private PM_PaperRepository paperRepository;
    @Autowired
    private PM_UserRepository userRepository;
    private PM_User testUser1;
    private PM_User testUser2;
    private PM_Paper testPaper;

    @BeforeEach
    void setUp() {
        testUser1 = new PM_User();
        testUser1.setId(12345);
        testUser1.setName("Test User");
        testUser1.setPassword("some_password");
        userRepository.save(testUser1);
        testUser2 = new PM_User();
        testUser2.setId(12345);
        testUser2.setName("Test User");
        testUser2.setPassword("some_password");
        userRepository.save(testUser2);
        testPaper = new PM_Paper();
        testPaper.setDoi("10.1234/test");
        testPaper.setTitle("Test Paper");
        testPaper.setStatus(PM_Paper.Status.notSubmit);
        paperRepository.save(testPaper);
    }
    @Test
    @Transactional
    void deleteAllByPaperId() {
        authorPaperRepository.deleteAll();
        PM_AuthorPaper paper1 = new PM_AuthorPaper();
        paper1.setPaperId("10.1234/test");
        paper1.setSeq(PM_AuthorPaper.Seq.second);
        paper1.setAuthorId(testUser1.getId());
        authorPaperRepository.save(paper1);

        PM_AuthorPaper paper2 = new PM_AuthorPaper();
        paper2.setPaperId("10.1234/test");
        paper2.setSeq(PM_AuthorPaper.Seq.second);
        paper2.setAuthorId(testUser2.getId());
        authorPaperRepository.save(paper2);

        authorPaperRepository.deleteAllByPaperId("10.1234/test");
        assertEquals(0, authorPaperRepository.findAll().size());
    }
}