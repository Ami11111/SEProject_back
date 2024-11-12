//package com.library.backend.controller;
//
//import com.library.backend.entity.PM_UserPaperDelete;
//import com.library.backend.repository.*;
//import com.library.backend.service.PaperService;
//import com.library.backend.utils.JwtUtil;
//import io.swagger.annotations.ApiOperation;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.HashMap;
//import java.util.Map;
//
//
//@RestController
//@RequestMapping("/api")
//public class PM_PaperDeleteApplicationController {
//
//    @Autowired
//    private PM_UserRepository userRepository;
//
//    @Autowired
//    private PM_AdminRepository adminRepository;
//
//    @Autowired
//    private PM_PaperAdditionalRepository paperAdditionalRepository;
//
//    @Autowired
//    private PM_UserPaperClaimRepository userPaperClaimRepository;
//
//    @Autowired
//    private PM_UserPaperDeleteRepository userPaperDeleteRepository;
//
//    @Autowired
//    private PaperService paperService;
//
//    @Autowired
//    private JwtUtil jwtUtil;
//
//    @PostMapping("/papers/request/delete")
//    @ApiOperation(value = "用户请求删除论文")
//    public ResponseEntity<Object> requestDeletePaper(@RequestBody PM_UserPaperDelete userPaperDelete) {
//        Map<String, Object> response = new HashMap<>();
//        try {
//            PM_UserPaperDelete savedDeleteRequest = userPaperDeleteRepository.save(userPaperDelete);
//            response.put("message", "Success");
//            response.put("deleteRequest", savedDeleteRequest);
//            return new ResponseEntity<>(response, HttpStatus.OK);
//        } catch (Exception e) {
//            response.put("message", e.toString());
//            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//        }
//    }
//
//    @GetMapping("/papers/request/delete")
//    @ApiOperation(value = "获取删除申请")
//    public ResponseEntity<Object> getDeleteRequest(@RequestParam int authorId, @RequestParam String paperDoi) {
//        Map<String, Object> response = new HashMap<>();
//        try {
//            PM_UserPaperDelete deleteRequest = userPaperDeleteRepository.findByAuthorIdAndPaperDoi(authorId, paperDoi);
//            if (deleteRequest != null) {
//                response.put("message", "Success");
//                response.put("deleteRequest", deleteRequest);
//                return new ResponseEntity<>(response, HttpStatus.OK);
//            } else {
//                response.put("message", "未找到该删除申请");
//                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
//            }
//        } catch (Exception e) {
//            response.put("message", e.toString());
//            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//        }
//    }
//
//    @DeleteMapping("/papers/request/delete")
//    @ApiOperation(value = "移除删除申请")
//    public ResponseEntity<Object> removeDeleteRequest(@RequestParam int authorId, @RequestParam String paperDoi) {
//        Map<String, Object> response = new HashMap<>();
//        try {
//            userPaperDeleteRepository.deleteByAuthorIdAndPaperDoi(authorId, paperDoi);
//            response.put("message", "Success");
//            return new ResponseEntity<>(response, HttpStatus.OK);
//        } catch (Exception e) {
//            response.put("message", e.toString());
//            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//        }
//    }
//
//    @PutMapping("/papers/request/delete")
//    @ApiOperation(value = "通过删除申请")
//    public ResponseEntity<Object> approveDeleteRequest(@RequestParam int authorId, @RequestParam String paperDoi) {
//        Map<String, Object> response = new HashMap<>();
//        try {
//            PM_UserPaperDelete deleteRequest = userPaperDeleteRepository.findByAuthorIdAndPaperDoi(authorId, paperDoi);
//            if (deleteRequest != null) {
//                userPaperDeleteRepository.deleteByAuthorIdAndPaperDoi(authorId, paperDoi);
//                //todo:删除数据
//                response.put("message", "Success");
//                response.put("deleteRequest", updatedDeleteRequest);
//                return new ResponseEntity<>(response, HttpStatus.OK);
//            } else {
//                response.put("message", "未找到该删除申请");
//                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
//            }
//        } catch (Exception e) {
//            response.put("message", e.toString());
//            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//        }
//    }
//
//
//}