package com.library.backend.service;

import com.library.backend.entity.PM_AuthorPaper;
import com.library.backend.entity.PM_AuthorPaperClaim;
import com.library.backend.repository.PM_AuthorPaperClaimRepository;
import com.library.backend.repository.PM_AuthorPaperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaperClaimApplicationService {
    @Autowired
    private PM_AuthorPaperRepository authorPaperRepository;

    @Autowired
    private PM_AuthorPaperClaimRepository authorPaperClaimRepository;



}
