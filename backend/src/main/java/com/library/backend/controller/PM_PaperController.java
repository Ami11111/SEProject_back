package com.library.backend.controller;

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
    private PaperService paperService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PaperUtil paperUtil;

    @PostMapping("/papers")
    @ApiOperation(value = "添加论文")
    public ResponseEntity<Object> insertPaper(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 401 无权限
            // 用户不存在，用户没有维护姓名信息，或用户不是该论文的作者
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
            ArrayList<String> firstAuthor = (ArrayList<String>) requestBody.get("firstAuthor");
            ArrayList<String> secondAuthor = (ArrayList<String>) requestBody.get("secondAuthor");
            ArrayList<String> thirdAuthor = (ArrayList<String>) requestBody.get("thirdAuthor");
            boolean isFirst = firstAuthor.contains(name);
            boolean isSecond = secondAuthor.contains(name);
            boolean isThird = thirdAuthor.contains(name);
            if (!(isFirst || isSecond || isThird)) {
                response.put("message", "Unauthorized");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }

            // paper数据组装
            PM_Paper paper = new PM_Paper();
            String doi = (String) requestBody.get("DOI");
            paper.setDoi(doi);
            paper.setTitle((String) requestBody.get("title"));
            paper.setCcf(PM_Paper.CCF.valueOf((String) requestBody.get("CCF")));
            paper.setUrl((String) requestBody.get("url"));
            paper.setStatus(PM_Paper.Status.valueOf((String) requestBody.get("status")));
            if (paper.getStatus() == PM_Paper.Status.reject)
                paper.setRecommend((String) requestBody.get("recommend"));
            if (!firstAuthor.isEmpty())
                paper.setFirstAuthor(String.join(",", firstAuthor));
            if (!secondAuthor.isEmpty())
                paper.setSecondAuthor(String.join(",", secondAuthor));
            if (!thirdAuthor.isEmpty())
                paper.setThirdAuthor(String.join(",", thirdAuthor));

            // paper_additional多条数据组装
            ArrayList<Map<String, String>> additional = (ArrayList<Map<String, String>>) requestBody.get("additional");
            ArrayList<PM_PaperAdditional> paperAdditionals=new ArrayList<>();
            for (Map<String, String> map : additional) {
                PM_PaperAdditional paperAdditional = new PM_PaperAdditional();
                paperAdditional.setDoi(doi);
                paperAdditional.setKey(PM_PaperAdditional.Key.valueOf(map.get("key")));
                paperAdditional.setValue(map.get("value"));
                paperAdditionals.add(paperAdditional);
            }

            // author_paper数据组装
            PM_AuthorPaper authorPaper = new PM_AuthorPaper();
            authorPaper.setAuthorId(id);
            authorPaper.setPaperId(doi);
            if (isFirst)
                authorPaper.setSeq(PM_AuthorPaper.Seq.first);
            else if (isSecond)
                authorPaper.setSeq(PM_AuthorPaper.Seq.second);
            else
                authorPaper.setSeq(PM_AuthorPaper.Seq.third);

            // 原子执行，失败回滚
            paperService.insertPaper(paper,paperAdditionals,authorPaper);

            // 200 成功
            response.put("message", "Paper inserted successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            // 其他异常，如enum型CCF、Status不匹配等
            return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/papers/{doi}")
    @ApiOperation(value = "删除论文")
    public ResponseEntity<Object> deletePaper(@RequestHeader("Authorization") String token, @PathVariable("doi") String doi) {
        Map<String,Object> response=new HashMap<>();
        System.out.println(doi);
        try{
            // 401 无权限
            // 非管理员
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
            response.put("message","Paper deleted successfully");
            return new ResponseEntity<>(response,HttpStatus.NO_CONTENT);

        }catch(Exception e){
            // 其他异常
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
            // 检查是否存在对应 DOI 的论文
            if (paperRepository.countByDoi(doi) == 0) {
                response.put("message", "No paper found with DOI: " + doi);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND); // 404 Not Found
            }
            // 检查用户是否为doi对应论文的作者
            String authorList = paperRepository.findByDoi(doi).get(0).getAuthorList();
            if (!authorList.contains(user.getName())) {
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


}