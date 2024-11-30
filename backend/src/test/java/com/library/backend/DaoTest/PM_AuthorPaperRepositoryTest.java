package com.library.backend.DaoTest;

import com.library.backend.entity.PM_AuthorPaper;
import com.library.backend.repository.PM_AuthorPaperRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PM_AuthorPaperRepositoryTest {

    @Autowired
    private PM_AuthorPaperRepository authorPaperRepository;

    @Test
    void deleteAllByPaperId() {
        PM_AuthorPaper paper1 = new PM_AuthorPaper();
        paper1.setPaperId("10.1234/test");
        authorPaperRepository.save(paper1);

        PM_AuthorPaper paper2 = new PM_AuthorPaper();
        paper2.setPaperId("10.1234/test");
        authorPaperRepository.save(paper2);

        authorPaperRepository.deleteAllByPaperId("10.1234/test");
        assertEquals(0, authorPaperRepository.findAll().size());
    }
}