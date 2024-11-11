package com.library.backend.controller;


import com.library.backend.entity.PM_Admin;
import com.library.backend.entity.PM_DeleteRequests;
import com.library.backend.repository.PM_AdminRepository;
import com.library.backend.repository.PM_DeleteRequestsRepository;
import com.library.backend.utils.JwtUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/papers")
public class PM_DeleteRequestsController {
    @Autowired
    private PM_DeleteRequestsRepository deleteRequestsRepository;

    @Autowired
    private PM_AdminRepository adminRepository;

    @Autowired
    private JwtUtil jwtUtil;

        @GetMapping("/request/delete")
    @ApiOperation(value = "获取删除申请")
    public ResponseEntity<Object> getDeleteRequest(@RequestHeader("Authorization") String token) {
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
            PM_Admin admin = adminRepository.findById(Integer.parseInt(id));
            if (admin == null) {
                response.put("message", "Access denied");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED); // 401 状态码
            }
            // 获取所有删除申请
            List<PM_DeleteRequests> deleteRequests = deleteRequestsRepository.findAll();
            response.put("message", "Success");
            response.put("deletes", deleteRequests);
            return new ResponseEntity<>(response, HttpStatus.OK); // 200 OK
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR); // 500 状态码
        }
    }

    @DeleteMapping("/request/delete")
    @ApiOperation(value = "移除删除申请")
    public ResponseEntity<Object> removeDeleteRequest(@RequestHeader("Authorization") String token,
                                                      @RequestParam("doi") String encodedDoi,
                                                      @RequestParam("id") int userId) {
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
            PM_Admin admin = adminRepository.findById(Integer.parseInt(id));
            if (admin == null) {
                response.put("message", "Access denied");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED); // 401 状态码
            }
            // 使用base64解码doi
            String doi = new String(java.util.Base64.getDecoder().decode(encodedDoi));
            // 删除删除申请
            int cnt = deleteRequestsRepository.deleteByDoiAndUserId(doi, userId);
            if (cnt == 0) {
                response.put("message", "No such delete request");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND); // 404 状态码
            }
            response.put("message", "Success");
            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT); // 204 状态码
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR); // 500 状态码
        }
    }
}
