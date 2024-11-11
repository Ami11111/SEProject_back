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
import com.library.backend.service.PaperService;
import com.library.backend.utils.JwtUtil;
import com.library.backend.utils.PaperUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.*;


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
    private PM_UserPaperClaimRepository userPaperClaimRepository;

    @Autowired
    private PM_UserPaperDeleteRepository userPaperDeleteRepository;

    @Autowired
    private PaperService paperService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PaperUtil paperUtil;


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
            int id = Integer.parseInt(jwtUtil.extractUsername(token));
            if (adminRepository.findById(id) == null) {
                response.put("message", "Unauthorized");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }

            // 404 论文不存在
            if (paperRepository.findByDoi(doi) == null) {
                response.put("message", "Paper not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            // 删除数据，原子执行
            paperService.deletePaper(doi);

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
    public ResponseEntity<Object> uploadPaperFile(@PathVariable("doi") String encodedDoi, @RequestParam("file") MultipartFile file, @RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 检验操作者是否为管理员
            // 去掉 Bearer 前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            // 从Token中解析用户id
            String id = jwtUtil.extractUsername(token);
            // 根据id查询数据库中的用户
            PM_User user = userRepository.findById(Integer.parseInt(id));
            if (user == null) {
                response.put("message", "Access denied");
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN); // 403 状态码
            }

            String doi = new String(Base64.getDecoder().decode(encodedDoi));
            List<PM_Paper> paper = paperRepository.findByDoi(doi);
            // 检查是否存在对应 DOI 的论文
            if (paper.isEmpty()) {
                response.put("message", "No paper found with DOI: " + doi);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND); // 404 Not Found
            }
            // 检查用户是否为doi对应论文的作者
            // 获取作者字段，并以半角逗号分隔成列表
            String[] firstAuthors = paper.get(0).getFirstAuthor().split(",");
            String[] secondAuthors = paper.get(0).getSecondAuthor().split(",");
            String[] thirdAuthors = paper.get(0).getThirdAuthor().split(",");
            boolean isAuthor = false;
            for (String author : firstAuthors) {
                if (author.trim().equals(user.getName())) {
                    isAuthor = true;
                    break;
                }
            }
            for (String author : secondAuthors) {
                if (author.trim().equals(user.getName())) {
                    isAuthor = true;
                    break;
                }
            }
            for (String author : thirdAuthors) {
                if (author.trim().equals(user.getName())) {
                    isAuthor = true;
                    break;
                }
            }
            if (!isAuthor) {
                response.put("message", "Access denied");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED); // 401 状态码
            }
            // 上传文件
            byte[] fileData = file.getBytes();
            paperRepository.updateFileDataByDoi(fileData, doi);

            response.put("message", "File uploaded successfully!");
            return new ResponseEntity<>(response, HttpStatus.OK); // 200 OK
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR); // 500 状态码
        }
    }



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
    public ResponseEntity<Object> findByDoi(@PathVariable String doi) {
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
    public ResponseEntity<Object> findByAuthor(@PathVariable String author) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<PM_Paper> papers = paperRepository.findByAuthorListContaining(author);
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
    @PostMapping("/papers/request/claim")
    @ApiOperation(value = "用户认领论文")
    public ResponseEntity<Object> claimPaper(@RequestBody PM_UserPaperClaim userPaperClaim) {
        Map<String, Object> response = new HashMap<>();
        try {
            userPaperClaimRepository.deleteByAuthorIdAndPaperDoi(userPaperClaim.authorId,userPaperClaim.paperDoi); // 设置初始状态为PENDING
            response.put("message", "Success");
            response.put("claim", savedClaim);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("message", e.toString());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/papers/request/claim")
    @ApiOperation(value = "删除认领申请")
    public ResponseEntity<Object> deleteClaim(@RequestParam int authorId, @RequestParam String paperDoi) {
        Map<String, Object> response = new HashMap<>();
        try {
            userPaperClaimRepository.deleteByAuthorIdAndPaperDoi(authorId, paperDoi);
            response.put("message", "Success");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("message", e.toString());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/papers/request/claim")
    @ApiOperation(value = "获取认领申请")
    public ResponseEntity<Object> getClaim(@RequestParam int authorId, @RequestParam String paperDoi) {
        Map<String, Object> response = new HashMap<>();
        try {
            PM_UserPaperClaim claim = userPaperClaimRepository.findByAuthorIdAndPaperDoi(authorId, paperDoi);
            if (claim != null) {
                response.put("message", "Success");
                response.put("claim", claim);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("message", "未找到该认领申请");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            response.put("message", e.toString());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/papers/request/delete")
    @ApiOperation(value = "通过认领申请")
    public ResponseEntity<Object> approveClaim(@RequestParam int authorId, @RequestParam String paperDoi) {
        Map<String, Object> response = new HashMap<>();
        try {
            PM_UserPaperClaim claim = userPaperClaimRepository.findByAuthorIdAndPaperDoi(authorId, paperDoi);
            if (claim != null) {
                userPaperClaimRepository.deleteByAuthorIdAndPaperDoi(authorId, paperDoi);
                response.put("message", "Success");
                response.put("claim", updatedClaim);
                //todo: 增加数据
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("message", "未找到该认领申请");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            response.put("message", e.toString());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping("/papers/request/delete")
    @ApiOperation(value = "用户请求删除论文")
    public ResponseEntity<Object> requestDeletePaper(@RequestBody PM_UserPaperDelete userPaperDelete) {
        Map<String, Object> response = new HashMap<>();
        try {
            PM_UserPaperDelete savedDeleteRequest = userPaperDeleteRepository.save(userPaperDelete);
            response.put("message", "Success");
            response.put("deleteRequest", savedDeleteRequest);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("message", e.toString());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/papers/request/delete")
    @ApiOperation(value = "获取删除申请")
    public ResponseEntity<Object> getDeleteRequest(@RequestParam int authorId, @RequestParam String paperDoi) {
        Map<String, Object> response = new HashMap<>();
        try {
            PM_UserPaperDelete deleteRequest = userPaperDeleteRepository.findByAuthorIdAndPaperDoi(authorId, paperDoi);
            if (deleteRequest != null) {
                response.put("message", "Success");
                response.put("deleteRequest", deleteRequest);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("message", "未找到该删除申请");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            response.put("message", e.toString());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/papers/request/delete")
    @ApiOperation(value = "移除删除申请")
    public ResponseEntity<Object> removeDeleteRequest(@RequestParam int authorId, @RequestParam String paperDoi) {
        Map<String, Object> response = new HashMap<>();
        try {
            userPaperDeleteRepository.deleteByAuthorIdAndPaperDoi(authorId, paperDoi);
            response.put("message", "Success");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("message", e.toString());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/papers/request/delete")
    @ApiOperation(value = "通过删除申请")
    public ResponseEntity<Object> approveDeleteRequest(@RequestParam int authorId, @RequestParam String paperDoi) {
        Map<String, Object> response = new HashMap<>();
        try {
            PM_UserPaperDelete deleteRequest = userPaperDeleteRepository.findByAuthorIdAndPaperDoi(authorId, paperDoi);
            if (deleteRequest != null) {
                userPaperDeleteRepository.deleteByAuthorIdAndPaperDoi(authorId, paperDoi);
                //todo:删除数据
                response.put("message", "Success");
                response.put("deleteRequest", updatedDeleteRequest);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response.put("message", "未找到该删除申请");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            response.put("message", e.toString());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

}