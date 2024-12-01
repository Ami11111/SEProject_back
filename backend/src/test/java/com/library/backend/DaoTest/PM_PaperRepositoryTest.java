package com.library.backend.DaoTest;

import com.library.backend.entity.PM_Paper;
import com.library.backend.repository.PM_PaperRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

import java.util.List;

@SpringBootTest
public class PM_PaperRepositoryTest {

    @Autowired
    private PM_PaperRepository paperRepository;

    @Test
    void add() {
        PM_Paper newPaper = new PM_Paper();
        newPaper.setDoi("10.1234/test");
        newPaper.setTitle("Test Paper");
        paperRepository.save(newPaper);
        int count = paperRepository.countByDoi("10.1234/test");
        assert count == 1;
    }

    @Test
    void countByDoi() {
        PM_Paper newPaper = new PM_Paper();
        newPaper.setDoi("10.1234/test");
        newPaper.setTitle("Test Paper");
        paperRepository.save(newPaper);
        int count = paperRepository.countByDoi("10.1234/test");
        assert count == 1;
        paperRepository.deleteByDoi("10.1234/test");
        count = paperRepository.countByDoi("10.1234/test");
        assert count == 0;
    }

    @Test
    void findAll() {
        PM_Paper newPaper = new PM_Paper();
        newPaper.setDoi("10.1234/test");
        newPaper.setTitle("Test Paper");
        paperRepository.save(newPaper);
        List<PM_Paper> papers = paperRepository.findAll();
        assertNotNull(papers);
        assertTrue(papers.size() > 0);
    }

    @Test
    void findByDoi() {
        PM_Paper newPaper = new PM_Paper();
        newPaper.setDoi("10.1234/test");
        newPaper.setTitle("Test Paper");
        paperRepository.save(newPaper);
        PM_Paper foundPaper = paperRepository.findByDoi("10.1234/test");
        assertNotNull(foundPaper);
        assertEquals("10.1234/test", foundPaper.getDoi());
        assertEquals("Test Paper", foundPaper.getTitle());
    }

    @Test
    void deleteByDoi() {
        PM_Paper newPaper = new PM_Paper();
        newPaper.setDoi("10.1234/test");
        newPaper.setTitle("Test Paper");
        paperRepository.save(newPaper);
        int count = paperRepository.countByDoi("10.1234/test");
        assert count == 1;
        paperRepository.deleteByDoi("10.1234/test");
        count = paperRepository.countByDoi("10.1234/test");
        assert count == 0;
    }

    @Test
    void updateUrlByDoi() {
        PM_Paper newPaper = new PM_Paper();
        newPaper.setDoi("10.1234/test");
        newPaper.setTitle("Test Paper");
        paperRepository.save(newPaper);
        paperRepository.updateUrlByDoi("http://newurl.com", "10.1234/test");
        PM_Paper updatedPaper = paperRepository.findByDoi("10.1234/test");
        assertEquals("http://newurl.com", updatedPaper.getUrl());
    }

    @Test
    void updateFileDataByDoi() {
        PM_Paper newPaper = new PM_Paper();
        newPaper.setDoi("10.1234/test");
        newPaper.setTitle("Test Paper");
        paperRepository.save(newPaper);
        byte[] fileData = "Test File Data".getBytes();
        paperRepository.updateFileDataByDoi(fileData, "10.1234/test");
        PM_Paper updatedPaper = paperRepository.findByDoi("10.1234/test");
        assertArrayEquals(fileData, updatedPaper.getFileData());
    }

    @Test
    void findPapersByUserIdAndDoi() {
        PM_Paper newPaper = new PM_Paper();
        newPaper.setDoi("10.1234/test");
        newPaper.setTitle("Test Paper");
        paperRepository.save(newPaper);
        List<PM_Paper> papers = paperRepository.findPapersByUserIdAndDoi(1, "10.1234/test");
        assertNotNull(papers);
        assertTrue(papers.size() > 0);
    }
}