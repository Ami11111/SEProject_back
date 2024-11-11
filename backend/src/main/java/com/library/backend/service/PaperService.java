package com.library.backend.service;

import com.library.backend.dto.PaperDTO;
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
import java.util.Map;

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
    public void deletePaper(String doi) {
        // paper_additional删除数据
        paperAdditionalRepository.deleteAllByDoi(doi);
        // author_paper删除数据
        authorPaperRepository.deleteAllByPaperId(doi);
        // paper删除数据
        paperRepository.deleteByDoi(doi);
    }

    public String getSeq(PaperDTO paperDTO, String name) {
        // 判断用户是否为论文作者
        if (paperDTO.getFirstAuthor().contains(name))
            return "first";
        else if (paperDTO.getSecondAuthor().contains(name))
            return "second";
        else if (paperDTO.getThirdAuthor().contains(name))
            return "third";
        else
            return null;
    }

    public PM_Paper paperDTOToPaper(PaperDTO paperDTO) {
        // 根据PaperDTO组装paper
        PM_Paper paper = new PM_Paper();
        paper.setDoi(paperDTO.getDOI());
        paper.setTitle(paperDTO.getTitle());
        if (paperDTO.getCCF() == null)
            paper.setCcf(null);
        else
            paper.setCcf(PM_Paper.CCF.valueOf(paperDTO.getCCF()));
        paper.setUrl(paperDTO.getUrl());
        paper.setStatus(PM_Paper.Status.valueOf(paperDTO.getStatus()));
        if (paper.getStatus() == PM_Paper.Status.reject)
            paper.setRecommend(paperDTO.getRecommend());
        ArrayList<String> firstAuthor = paperDTO.getFirstAuthor();
        if (!firstAuthor.isEmpty())
            paper.setFirstAuthor(String.join(",", firstAuthor));
        ArrayList<String> secondAuthor = paperDTO.getSecondAuthor();
        if (!secondAuthor.isEmpty())
            paper.setSecondAuthor(String.join(",", secondAuthor));
        ArrayList<String> thirdAuthor = paperDTO.getThirdAuthor();
        if (!thirdAuthor.isEmpty())
            paper.setThirdAuthor(String.join(",", thirdAuthor));
        return paper;
    }

    public ArrayList<PM_PaperAdditional> paperDTOToPaperAdditionals(PaperDTO paperDTO) {
        // 根据paperDTO组装paperAdditionals
        ArrayList<PM_PaperAdditional> paperAdditionals = new ArrayList<>();
        String doi = paperDTO.getDOI();
        for (Map<String, String> map : paperDTO.getAdditional()) {
            PM_PaperAdditional paperAdditional = new PM_PaperAdditional();
            paperAdditional.setDoi(doi);
            paperAdditional.setKey(PM_PaperAdditional.Key.valueOf(map.get("key")));
            paperAdditional.setValue(map.get("value"));
            paperAdditionals.add(paperAdditional);
        }
        return paperAdditionals;
    }

}
