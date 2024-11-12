package com.library.backend.service;

import com.library.backend.entity.PM_User;
import com.library.backend.repository.PM_UserRepository;
import com.library.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class JwtService {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PM_UserRepository userRepository;

    public ResponseEntity<Object> isAdmin(String token, Map<String, Object> response) {
        // 检验操作者是否为管理员
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        String id = jwtUtil.extractUsername(token);
        // 根据id查询数据库中的用户
        PM_User user = userRepository.findById(Integer.parseInt(id));
        if (user == null) {
            response.put("message", "Access denied");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        return null;
    }
}
