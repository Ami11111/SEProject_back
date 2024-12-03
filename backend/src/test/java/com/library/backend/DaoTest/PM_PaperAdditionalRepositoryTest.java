package com.library.backend.DaoTest;

import com.library.backend.entity.PM_PaperAdditional;
import com.library.backend.repository.PM_PaperAdditionalRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PM_PaperAdditionalRepositoryTest {

    @Autowired
    private PM_PaperAdditionalRepository paperAdditionalRepository;

    @Test
    void deleteAllByDoi() {
        PM_PaperAdditional additional1 = new PM_PaperAdditional();
        additional1.setDoi("10.1234/test");
        additional1.setKey(PM_PaperAdditional.Key.correspondingAuthor);
        additional1.setValue("test");
        paperAdditionalRepository.save(additional1);

        PM_PaperAdditional additional2 = new PM_PaperAdditional();
        additional2.setDoi("10.1234/test");
        additional2.setKey(PM_PaperAdditional.Key.correspondingAuthor);
        additional2.setValue("test");
        paperAdditionalRepository.save(additional2);

        paperAdditionalRepository.deleteAllByDoi("10.1234/test");
        List<PM_PaperAdditional> foundAdditionals = paperAdditionalRepository.findByDoi("10.1234/test");
        assertTrue(foundAdditionals.isEmpty());
    }

    @Test
    void findByDoi() {
        PM_PaperAdditional additional1 = new PM_PaperAdditional();
        additional1.setDoi("10.1234/test");
        additional1.setKey(PM_PaperAdditional.Key.correspondingAuthor);
        additional1.setValue("test");
        paperAdditionalRepository.save(additional1);
        List<PM_PaperAdditional> foundAdditionals = paperAdditionalRepository.findByDoi("10.1234/test");
        assertEquals(1, foundAdditionals.size());
    }
}