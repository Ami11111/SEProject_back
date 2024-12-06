package com.library.backend.ServiceTest;

import com.library.backend.entity.PM_AuthorPaper;
import com.library.backend.entity.PM_Paper;
import com.library.backend.entity.PM_PaperAdditional;
import com.library.backend.repository.PM_AuthorPaperRepository;
import com.library.backend.repository.PM_PaperAdditionalRepository;
import com.library.backend.repository.PM_PaperRepository;
import com.library.backend.service.PaperService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class PaperServiceTest {

    @Autowired
    private PaperService paperService;

    @Autowired
    private PM_PaperRepository paperRepository;

    @Autowired
    private PM_PaperAdditionalRepository paperAdditionalRepository;

    @Autowired
    private PM_AuthorPaperRepository authorPaperRepository;

    @Test
    public void testInsertPaperWithAuthor() {
        PM_Paper paper = new PM_Paper();
        paper.setDoi("10.5678/test.doi");
        paper.setTitle("Test Paper");
        paper.setFirstAuthor("chen");
        paper.setSecondAuthor("feng");
        paper.setThirdAuthor("liang");
        paper.setCcf(PM_Paper.CCF.A);
        paper.setStatus(PM_Paper.Status.review);

        ArrayList<PM_PaperAdditional> paperAdditionals = new ArrayList<>();
        PM_PaperAdditional additional = new PM_PaperAdditional();
        additional.setDoi(paper.getDoi());
        additional.setKey(PM_PaperAdditional.Key.conferenceOrPeriodical);
        additional.setValue("Test Conference");
        paperAdditionals.add(additional);

        PM_AuthorPaper authorPaper = new PM_AuthorPaper();
        authorPaper.setPaperId(paper.getDoi());
        authorPaper.setAuthorId(21808081);
        authorPaper.setSeq(PM_AuthorPaper.Seq.valueOf("first"));

        paperService.insertPaper(paper, paperAdditionals, authorPaper);

        // 验证数据是否正确保存
        Optional<PM_Paper> savedPaper = paperRepository.findById("10.5678/test.doi");
        assertTrue(savedPaper.isPresent());
        assertEquals("Test Paper", savedPaper.get().getTitle());

        List<PM_PaperAdditional> savedAdditionals = paperAdditionalRepository.findByDoi("10.5678/test.doi");
        assertEquals(1, savedAdditionals.size());
        assertEquals("Test Conference", savedAdditionals.get(0).getValue());

        //List<PM_AuthorPaper> savedAuthors = authorPaperRepository.findByPaperId("10.5678/test.doi");
        //assertEquals(1, savedAuthors.size());
        //assertEquals(21808081, savedAuthors.get(0).getAuthorId());
    }

    @Test
    public void testIsAuthorOfPaper() {
        String doi = "10.1016/j.artint.2023.104057";
        String userName = "chen";

        Optional<PM_Paper> paper = paperRepository.findById(doi);
        assertTrue(paper.isPresent());

        boolean isAuthor = paperService.isAuthorOfPaper(userName, paper.get().getFirstAuthor(), paper.get().getSecondAuthor(), paper.get().getThirdAuthor());
        assertTrue(isAuthor);
    }

    @Test
    public void testUploadFile() throws IOException {
        String doi = "10.1016/j.artint.2023.104057";
        byte[] fileData = "Test PDF Content".getBytes();
        MultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", fileData);

        paperService.uploadFile(file, doi);

        Optional<PM_Paper> paper = paperRepository.findById(doi);
        assertTrue(paper.isPresent());
        assertNotNull(paper.get().getFileData());

        String expectedDownloadUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/files/download/")
                .path(Base64.getUrlEncoder().encodeToString(doi.getBytes()))
                .toUriString();

        assertEquals(expectedDownloadUrl, paper.get().getUrl());
    }

    @Test
    public void testGetSeq() {
        String doi = "10.1016/j.artint.2023.104057";
        String userName = "chen";

        Optional<PM_Paper> paper = paperRepository.findById(doi);
        assertTrue(paper.isPresent());

        String seq = paperService.getSeq(paper.get(), userName);
        assertEquals("first", seq);
    }
}
