package com.library.backend.service;

import com.library.backend.entity.PM_AuthorPaper;
import com.library.backend.entity.PM_Paper;
import com.library.backend.entity.PM_PaperAdditional;
import com.library.backend.repository.PM_AuthorPaperRepository;
import com.library.backend.repository.PM_PaperAdditionalRepository;
import com.library.backend.repository.PM_PaperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;

@Service
public class PaperService {

    @Autowired
    private PM_PaperRepository paperRepository;

    @Autowired
    private PM_PaperAdditionalRepository paperAdditionalRepository;

    @Autowired
    private PM_AuthorPaperRepository authorPaperRepository;

    @Transactional
    public void insertPaper(PM_Paper paper, ArrayList<PM_PaperAdditional> paperAdditionals, PM_AuthorPaper authorPaper) {
        paperRepository.save(paper); // save插入数据，或更新数据（有，则覆盖）
        // 删除原先的paper_additional数据
        paperAdditionalRepository.deleteAllByDoi(paper.getDoi());
        // 插入新的paper_additional数据
        paperAdditionalRepository.saveAll(paperAdditionals);
        // 已关联的作者-论文不会被删除
        authorPaperRepository.save(authorPaper);
    }

    @Transactional
    public void deletePaper(String doi){
        // paper_additional删除数据
        paperAdditionalRepository.deleteAllByDoi(doi);
        // author_paper删除数据
        authorPaperRepository.deleteAllByPaperId(doi);
        // paper删除数据
        paperRepository.deleteByDoi(doi);
    }
}
