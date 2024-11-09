package com.library.backend.controller;

import com.library.backend.entity.PM_Admin;
import com.library.backend.entity.PM_Paper;
import com.library.backend.entity.PM_User;
import com.library.backend.repository.PM_PaperRepository;
import com.library.backend.repository.PM_UserRepository;
import com.library.backend.utils.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@Api(tags = "论文管理")
@RequestMapping("/api/papers")
public class PM_PaperController {
    @Autowired
    private PM_PaperRepository paperRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PM_UserRepository userRepository;

    @PostMapping("/{doi}/file")
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
            if (user==null) {
                response.put("message", "Access denied");
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN); // 403 状态码
            }

            String doi = new String(Base64.getDecoder().decode(encodedDoi));
            // 检查是否存在对应 DOI 的论文
            if (paperRepository.countByDoi(doi) == 0) {
                response.put("message", "No paper found with DOI: " + doi);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND); // 404 Not Found
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
}
