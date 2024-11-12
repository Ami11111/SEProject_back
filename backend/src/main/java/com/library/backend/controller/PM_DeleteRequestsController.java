package com.library.backend.controller;


import com.library.backend.dto.DeleteRequestsDTO;
import com.library.backend.entity.PM_Admin;
import com.library.backend.entity.PM_DeleteRequests;
import com.library.backend.entity.PM_Paper;
import com.library.backend.entity.PM_User;
import com.library.backend.repository.PM_AdminRepository;
import com.library.backend.repository.PM_DeleteRequestsRepository;
import com.library.backend.repository.PM_PaperRepository;
import com.library.backend.repository.PM_UserRepository;
import com.library.backend.service.PaperService;
import com.library.backend.utils.JwtUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/papers")
public class PM_DeleteRequestsController {
    @Autowired
    private PM_DeleteRequestsRepository deleteRequestsRepository;

    @Autowired
    private PM_AdminRepository adminRepository;

    @Autowired
    private PM_UserRepository userRepository;

    @Autowired
    private PM_PaperRepository paperRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PaperService paperService;

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
            List<DeleteRequestsDTO> deleteRequestsDTO = deleteRequests.stream()
                            .map(deleteRequest -> new DeleteRequestsDTO(String.valueOf(deleteRequest.getUserId()), deleteRequest.getDoi()))
                    .collect(Collectors.toList());
            response.put("message", "Success");
            response.put("deletes", deleteRequestsDTO);
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
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            String id = jwtUtil.extractUsername(token);
            PM_Admin admin = adminRepository.findById(Integer.parseInt(id));
            if (admin == null) {
                response.put("message", "Access denied");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED); // 401 状态码
            }
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

    @PostMapping("/request/delete")
    @ApiOperation(value = "用户请求删除论文")
    public ResponseEntity<Object> requestDeletePaper(@RequestHeader("Authorization") String token,@RequestParam("doi") String encodedDoi) {
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
            String doi = new String(java.util.Base64.getDecoder().decode(encodedDoi));
            PM_Paper paper = paperRepository.findByDoi(doi);
            // 检查是否存在对应 DOI 的论文
            if (paper==null) {
                response.put("message", "No paper found with DOI: " + doi);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND); // 404 Not Found
            }
            // 判断用户是否为论文的作者
            boolean isAuthor = paperService.isAuthorOfPaper(user.getName(), paper.getFirstAuthor(), paper.getSecondAuthor(), paper.getThirdAuthor());
            if (!isAuthor) {
                response.put("message", "You are not the author of this paper");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED); // 401 状态码
            }
            // 创建删除申请
            PM_DeleteRequests deleteRequest = new PM_DeleteRequests();
            deleteRequest.setDoi(doi);
            deleteRequest.setUserId(Integer.parseInt(id));
            deleteRequestsRepository.save(deleteRequest);
            response.put("message", "Success");
            return new ResponseEntity<>(response, HttpStatus.CREATED); // 201 状态码
        } catch (Exception e) {
            response.put("message", e.toString());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
}
