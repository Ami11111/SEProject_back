package com.library.backend.controller;

import com.library.backend.dto.PaperDTO;
import com.library.backend.entity.PM_AuthorPaper;
import com.library.backend.entity.PM_Paper;
import com.library.backend.entity.PM_PaperAdditional;
import com.library.backend.entity.PM_User;
import com.library.backend.entity.PM_Admin;
import com.library.backend.repository.PM_AuthorPaperRepository;
import com.library.backend.repository.PM_PaperAdditionalRepository;
import com.library.backend.repository.PM_PaperRepository;
import com.library.backend.repository.PM_UserRepository;
import com.library.backend.repository.PM_AdminRepository;
import com.library.backend.service.JwtService;
import com.library.backend.service.PaperService;
import com.library.backend.utils.JwtUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api")
public class PM_PaperController {
    @Autowired
    private PM_PaperRepository paperRepository;

    @Autowired
    private PM_UserRepository userRepository;

    @Autowired
    private PM_AdminRepository adminRepository;

    @Autowired
    private PM_AuthorPaperRepository authorPaperRepository;

    @Autowired
    private PM_PaperAdditionalRepository paperAdditionalRepository;

    @Autowired
    private PaperService paperService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/papers")
    @ApiOperation(value = "添加论文")
    public ResponseEntity<Object> insertPaper(@Valid @RequestHeader("Authorization") String token, @RequestBody PaperDTO paperDTO) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 401 无权限 用户不存在，用户没有维护姓名信息
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            int id = Integer.parseInt(jwtUtil.extractUsername(token));
            PM_User user = userRepository.findById(id);
            if (user == null) {
                response.put("message", "Unauthorized");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
            String name = user.getName();
            if (name == null) {
                response.put("message", "Unauthorized");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }

            String status = paperDTO.getStatus();
            switch (status) {
                // notSubmit仅插入paper和paper_additional
                case "notSubmit": {
                    PM_Paper paper = paperService.paperDTOToPaper(paperDTO);
                    ArrayList<PM_PaperAdditional> paperAdditionals = paperService.paperDTOToPaperAdditionals(paperDTO);
                    // 原子执行，失败回滚
                    paperService.insertPaper(paper, paperAdditionals);
                    // 200 成功
                    response.put("message", "Paper inserted successfully");
                    return new ResponseEntity<>(response, HttpStatus.OK);
                }
                // review插入paper、paper_additional、author_paper
                case "review": {
                    String seq = paperService.getSeq(paperDTO, name);
                    // 401 无权限 用户不是论文作者
                    if (seq == null) {
                        response.put("message", "Unauthorized");
                        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
                    }
                    PM_Paper paper = paperService.paperDTOToPaper(paperDTO);
                    ArrayList<PM_PaperAdditional> paperAdditionals = paperService.paperDTOToPaperAdditionals(paperDTO);
                    PM_AuthorPaper authorPaper = new PM_AuthorPaper();
                    authorPaper.setAuthorId(id);
                    authorPaper.setPaperId(paperDTO.getDOI());
                    authorPaper.setSeq(PM_AuthorPaper.Seq.valueOf(seq));
                    paperService.insertPaper(paper, paperAdditionals, authorPaper);
                    // 200 成功
                    response.put("message", "Paper inserted successfully");
                    return new ResponseEntity<>(response, HttpStatus.OK);
                }
                default:
                    // null,approve,reject
                    response.put("message", "Unauthorized");
                    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            // 其他异常，如enum型CCF、Status不匹配等
            return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/papers/{doi}")
    @ApiOperation(value = "删除论文")
    public ResponseEntity<Object> deletePaper(@RequestHeader("Authorization") String token, @PathVariable("doi") String doi) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 401 无权限 非管理员
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            int adminId = Integer.parseInt(jwtUtil.extractUsername(token));
            if (adminRepository.findById(adminId) == null) {
                response.put("message", "Unauthorized");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }

            // 404 论文不存在
            doi = new String(Base64.getDecoder().decode(doi));
            if (paperRepository.findByDoi(doi) == null) {
                response.put("message", "Paper not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            // 删除数据
            paperRepository.deleteByDoi(doi);

            // 204 删除成功
            response.put("message", "Paper deleted successfully");
            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);

        } catch (Exception e) {
            // 其他异常
            return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/papers/{doi}")
    @ApiOperation(value = "修改论文")
    public ResponseEntity<Object> updatePaper(@Valid @RequestHeader("Authorization") String token, @PathVariable("doi") String doi, @RequestBody PaperDTO paperDTO) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 401 无权限 用户或管理员不存在
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            int id = Integer.parseInt(jwtUtil.extractUsername(token));
            PM_User user = userRepository.findById(id);
            PM_Admin admin = adminRepository.findById(id);
            if (user == null && admin == null) {
                response.put("message", "Unauthorized");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }

            // 404 论文不存在
            doi = new String(Base64.getDecoder().decode(doi));
            if (paperRepository.findByDoi(doi) == null) {
                response.put("message", "Paper not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            // 管理员修改，status只能为approve或reject
            // 只插入paper、paper_additional
            else if (admin != null) {
                String status = paperDTO.getStatus();
                if (status != "approve" && status != "reject") {
                    response.put("message", "Unauthorized");
                    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
                }
                PM_Paper paper = paperService.paperDTOToPaper(paperDTO);
                ArrayList<PM_PaperAdditional> paperAdditionals = paperService.paperDTOToPaperAdditionals(paperDTO);
                paperService.insertPaper(paper, paperAdditionals);
                response.put("message", "Paper updated successfully");
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
            // 用户修改，status只能notSubmit或review
            else {
                // 401 无权限 用户没有维护姓名信息
                String name = user.getName();
                if (name == null) {
                    response.put("message", "Unauthorized");
                    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
                }

                String status = paperDTO.getStatus();
                switch (status) {
                    // notSubmit仅插入paper和paper_additional
                    case "notSubmit": {
                        PM_Paper paper = paperService.paperDTOToPaper(paperDTO);
                        ArrayList<PM_PaperAdditional> paperAdditionals = paperService.paperDTOToPaperAdditionals(paperDTO);
                        // 原子执行，失败回滚
                        paperService.insertPaper(paper, paperAdditionals);
                        // 200 成功
                        response.put("message", "Paper updated successfully");
                        return new ResponseEntity<>(response, HttpStatus.OK);
                    }
                    // review插入paper、paper_additional、author_paper
                    case "review": {
                        String seq = paperService.getSeq(paperDTO, name);
                        // 401 无权限 用户不是论文作者
                        if (seq == null) {
                            response.put("message", "Unauthorized");
                            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
                        }
                        PM_Paper paper = paperService.paperDTOToPaper(paperDTO);
                        ArrayList<PM_PaperAdditional> paperAdditionals = paperService.paperDTOToPaperAdditionals(paperDTO);
                        PM_AuthorPaper authorPaper = new PM_AuthorPaper();
                        authorPaper.setAuthorId(id);
                        authorPaper.setPaperId(paperDTO.getDOI());
                        authorPaper.setSeq(PM_AuthorPaper.Seq.valueOf(seq));
                        paperService.insertPaper(paper, paperAdditionals, authorPaper);
                        // 200 成功
                        response.put("message", "Paper updated successfully");
                        return new ResponseEntity<>(response, HttpStatus.OK);
                    }
                    default:
                        response.put("message", "Unauthorized");
                        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
                }
            }
        } catch (Exception e) {
            // 其他异常，如enum型CCF、Status不匹配等
            return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/papers/{doi}/file")
    @ApiOperation(value = "上传论文文件")
    public ResponseEntity<Object> uploadPaperFile(@PathVariable("doi") String encodedDoi,
                                                  @RequestParam("file") MultipartFile file,
                                                  @RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            String id = jwtUtil.extractUsername(token);
            PM_User user = userRepository.findById(Integer.parseInt(id));
            if (user == null) {
                response.put("message", "Access denied");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED); // 401 状态码
            }

            String doi = new String(Base64.getDecoder().decode(encodedDoi));
            PM_Paper paper = paperRepository.findByDoi(doi);
            // 检查是否存在对应 DOI 的论文
            if (paper==null) {
                response.put("message", "No paper found with DOI: " + doi);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND); // 404 Not Found
            }
            // 判断用户是否为论文的作者
            boolean isAuthor = paperService.isAuthorOfPaper(user.getName(),
                    paper.getFirstAuthor(), paper.getSecondAuthor(), paper.getThirdAuthor());
            if (!isAuthor) {
                response.put("message", "Access denied");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED); // 401 状态码
            }
            // 上传文件并设置url
            paperService.uploadFile(file, doi);

            response.put("message", "File uploaded successfully!");
            return new ResponseEntity<>(response, HttpStatus.OK); // 200 OK
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR); // 500 状态码
        }
    }

    @GetMapping("/papers")
    @ApiOperation(value = "查询所有论文")
    public ResponseEntity<Object> findAll(@RequestHeader("Authorization") String token,
                                          @RequestParam("doi") String encodedDoi,
                                          @RequestParam("id") String userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            String id = jwtUtil.extractUsername(token);
            PM_User user = userRepository.findById(Integer.parseInt(id));
            if (user == null) {
                if (jwtService.isAdmin(token, response) == null) return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }

            String doi = null;
            Integer userIdInt = null;
            if (!encodedDoi.isEmpty()) {
                doi = new String(Base64.getDecoder().decode(encodedDoi));
            }
            if (!userId.isEmpty()) {
                userIdInt = Integer.parseInt(userId);
            }

            List<PM_Paper> papers = paperRepository.findPapersByUserIdAndDoi(userIdInt, doi);
            List<Map<String, Object>> papersWithAuthorsArray = papers.stream().map(paper -> {
                Map<String, Object> paperData = new HashMap<>();
                paperData.put("DOI", paper.getDoi());
                paperData.put("title", paper.getTitle());
                paperData.put("firstAuthor", paper.getFirstAuthorsArray());
                paperData.put("secondAuthor", paper.getSecondAuthorsArray());
                paperData.put("thirdAuthor", paper.getThirdAuthorsArray());
                paperData.put("CCF", paper.getCcf());
                paperData.put("status", paper.getStatus());
                paperData.put("recommend", paper.getRecommend());
                // 假设获取的 PM_PaperAdditional 列表
                List<PM_PaperAdditional> additionalList = paperAdditionalRepository.findByDoi(paper.getDoi());

                // 移除每个对象中的 doi 字段
                List<Map<String, String>> additionalWithoutDoi = additionalList.stream()
                    .map(PM_PaperAdditional::toMapWithoutDoi)
                    .collect(Collectors.toList());
                paperData.put("additional", additionalWithoutDoi);
                paperData.put("url", paper.getUrl());

                return paperData;
            }).collect(Collectors.toList());

            response.put("message", "Success");
            response.put("papers", papersWithAuthorsArray);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("message", e.toString());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/files/download/{doi}")
    @ApiOperation(value = "下载论文文件")
    public ResponseEntity<Object> downloadPaperFile(@PathVariable("doi") String encodedDoi,
                                                    @RequestHeader("Authorization") String token){
        Map<String, Object> response = new HashMap<>();
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            String id = jwtUtil.extractUsername(token);
            PM_User user = userRepository.findById(Integer.parseInt(id));
            if (user == null) {
                if (jwtService.isAdmin(token, response) == null) return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
            String doi = new String(Base64.getDecoder().decode(encodedDoi));
            PM_Paper paper = paperRepository.findByDoi(doi);
            if (paper == null) {
                response.put("message", "No file found with DOI: " + doi);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND); // 404 Not Found
            }
            byte[] fileData = paper.getFileData();

            // 文件流和内容类型设置
            InputStream inputStream = new ByteArrayInputStream(fileData);
            InputStreamResource resource = new InputStreamResource(inputStream);

            // 设置 HTTP 响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", paper.getDoi()); // 设置文件名
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM); // 设置内容类型为下载文件
            return new ResponseEntity<>(resource, headers, HttpStatus.OK);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR); // 500 状态码
        }
    }

}