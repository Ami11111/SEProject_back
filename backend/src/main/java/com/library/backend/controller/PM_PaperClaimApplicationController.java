package com.library.backend.controller;

import com.library.backend.entity.PM_Admin;
import com.library.backend.entity.PM_UserPaperClaim;
import com.library.backend.repository.*;
import com.library.backend.service.PaperService;
import com.library.backend.utils.JwtUtil;
import com.library.backend.utils.PaperUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api")
public class PM_PaperClaimApplicationController {
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


}