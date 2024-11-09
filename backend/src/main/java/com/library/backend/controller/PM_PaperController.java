package com.library.backend.controller;

import com.library.backend.entity.PM_Paper;
import com.library.backend.model.Result;
import com.library.backend.repository.PM_PaperRepository;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.library.backend.utils.Constant.*;

@RestController
@RequestMapping("/api")
public class PM_PaperController {

    @Autowired
    private PM_PaperRepository paperRepository;

    @GetMapping("/papers")
    @ApiOperation(value = "查询所有论文")
    public ResponseEntity<Object> findAll() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<PM_Paper> papers = paperRepository.findAll();
            response.put("message", "Success");
            response.put("papers", papers);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("message", e.toString());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/papers/{doi}")
    @ApiOperation(value = "根据DOI查询论文")
    public ResponseEntity<Object> findByDoi(@RequestParam String doi) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<PM_Paper> paperList = paperRepository.findByDoi(doi);
            
            if (!paperList.isEmpty()) {
                response.put("message", "Success");
                response.put("paper", paperList);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("message", "未找到该论文");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            response.put("message", e.toString());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/papers/{author}")
    @ApiOperation(value = "根据作者查询论文")
    public ResponseEntity<Object> findByAuthor(@RequestParam String author) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<PM_Paper> papers = paperRepository.findByAuthorListContaining(author, author, author); 
            if (!papers.isEmpty()) {
                response.put("message", "Success");
                response.put("papers", papers);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("message", "未找到该作者的论文");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            response.put("message", e.toString());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
}