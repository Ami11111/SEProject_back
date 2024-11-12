package com.library.backend.controller;

import com.library.backend.entity.PM_AuthorPaper;
import com.library.backend.entity.PM_Paper;
import com.library.backend.entity.PM_User;
import com.library.backend.entity.PM_AuthorPaperClaim;
import com.library.backend.repository.*;
import com.library.backend.service.PaperClaimApplicationService;
import com.library.backend.service.PaperService;
import com.library.backend.utils.JwtUtil;
import com.library.backend.utils.PaperUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;


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
    private PM_AuthorPaperClaimRepository authorPaperClaimRepository;

    @Autowired
    private PM_UserPaperDeleteRepository userPaperDeleteRepository;

    @Autowired
    private PaperService paperService;

    @Autowired
    private PaperClaimApplicationService paperClaimApplicationService;

    @Autowired
    private JwtUtil jwtUtil;


    @Autowired
    private PaperUtil paperUtil;


    @PostMapping("/papers/request/claim")
    @ApiOperation(value = "用户认领论文")
    public ResponseEntity<Object> claimPaper(@RequestHeader("Authorization") String token, @RequestParam("doi") String doi) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 401 无权限 用户不存在，用户没有维护姓名信息
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            int userId = Integer.parseInt(jwtUtil.extractUsername(token));
            PM_User user = userRepository.findById(userId);
            if (user == null) {
                response.put("message", "Unauthorized");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
            String name = user.getName();
            if (name == null) {
                response.put("message", "Unauthorized");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }

            // 404 论文不存在
            doi = Arrays.toString(Base64.getDecoder().decode(doi));
            PM_Paper paper = paperRepository.findByDoi(doi);
            if (paper == null) {
                response.put("message", "Paper not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            // 401 无权限 用户不是论文作者
            String seq = paperService.getSeq(paper, name);
            if (seq == null) {
                response.put("message", "Unauthorized");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }

            // 200 成功
            PM_AuthorPaperClaim userPaperClaim = new PM_AuthorPaperClaim();
            userPaperClaim.setAuthorId(userId);
            userPaperClaim.setPaperDoi(doi);
            authorPaperClaimRepository.save(userPaperClaim);
            response.put("message", "Claim applied successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            // 其他异常
            return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/papers/request/claim")
    @ApiOperation(value = "获取认领申请")
    public ResponseEntity<Object> getClaim(@RequestHeader("Authorization") String token) {
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

            List<PM_AuthorPaperClaim> paperClaims = authorPaperClaimRepository.findAll();
            ArrayList<Map<String, Object>> claims = new ArrayList<>();
            for (PM_AuthorPaperClaim paperClaim : paperClaims) {
                Map<String, Object> claim = new HashMap<>();
                claim.put("id", paperClaim.getAuthorId());
                claim.put("doi", paperClaim.getPaperDoi());
                claims.add(claim);
            }
            // 200 成功
            response.put("message", "Success");
            response.put("claims", claims);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            // 其他异常
            return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/papers/author")
    @ApiOperation(value = "添加论文作者")
    public ResponseEntity<Object> approveClaim(@RequestHeader("Authorization") String token, @RequestParam int authorId, @RequestParam String doi) {
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

            // 404 认领申请不存在
            doi = Arrays.toString(Base64.getDecoder().decode(doi));
            PM_AuthorPaperClaim userPaperClaim = authorPaperClaimRepository.findByAuthorIdAndPaperDoi(authorId, doi);
            if (userPaperClaim == null) {
                response.put("message", "Claim application not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            // 404 论文不存在
            PM_Paper paper = paperRepository.findByDoi(doi);
            if (paper == null) {
                response.put("message", "Paper not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            PM_User author = userRepository.findById(authorId);
            String name = author.getName();
            String seq = paperService.getSeq(paper, name);
            PM_AuthorPaper authorPaper = new PM_AuthorPaper();
            authorPaper.setAuthorId(authorId);
            authorPaper.setPaperId(doi);
            authorPaper.setSeq(PM_AuthorPaper.Seq.valueOf(seq));
            paperClaimApplicationService.approveClaim(authorPaper,userPaperClaim);

            // 200 成功
            response.put("message", "Claim approved successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            // 其他异常
            return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/papers/request/claim")
    @ApiOperation(value = "删除认领申请")
    public ResponseEntity<Object> deleteClaim(@RequestHeader("Authorization") String token, @RequestParam("id") int authorId, @RequestParam("doi") String doi) {
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

            // 404 认领申请不存在
            doi = Arrays.toString(Base64.getDecoder().decode(doi));
            if (authorPaperClaimRepository.findByAuthorIdAndPaperDoi(authorId, doi) == null) {
                response.put("message", "Claim application not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            // 204 删除成功
            authorPaperClaimRepository.deleteByAuthorIdAndPaperDoi(authorId, doi);
            response.put("message", "Claim application deleted successfully");
            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            // 其他异常
            return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}