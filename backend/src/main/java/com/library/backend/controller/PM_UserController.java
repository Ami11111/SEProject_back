package com.library.backend.controller;

import com.library.backend.entity.PM_User;
import com.library.backend.model.Result;
import com.library.backend.repository.PM_UserRepository;
import com.library.backend.utils.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.library.backend.utils.Constant.*;

@RestController
@Api(tags = "登录验证与用户管理")
@RequestMapping("/api")
public class PM_UserController {

    @Autowired
    private PM_UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    @ApiOperation(value = "用户登录")
    public ResponseEntity<Object> login(@RequestBody PM_User user) {
        try {
            // Map存储返回信息的键值对，Spring框架会自动转换为JSON格式返回给前端
            Map<String, Object> response = new HashMap<>();
            int cnt = userRepository.countByIdAndPassword(user.getId(), user.getPassword());
            if (cnt == 0) {
                response.put("message", "Invalid ID or password");
                // 返回一个 ResponseEntity 对象，包含响应体和状态码
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED); // 401 状态码
            }
            PM_User returnUser = userRepository.findById(user.getId());
            // 根据用户名生成 JWT Token， 只在登录成功时生成，后续操作由JWT过滤器自动校验前端发来的Token
            String jwtToken = jwtUtil.generateToken(String.valueOf(returnUser.getId()));
            response.put("token", jwtToken);
            returnUser.setPassword("");
            response.put("user", returnUser);
            // 返回一个 ResponseEntity 对象，包含响应体和状态码
            return new ResponseEntity<>(response, HttpStatus.OK); // 200 状态码
        } catch (Exception e) {

            return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR); // 500 状态码
        }
    }

    @PostMapping("/register")
    @ApiOperation(value = "用户注册")
    public ResponseEntity<Object> register(@RequestBody Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();
        try {
            Object idObject = requestBody.get("id");
            // 检查ID是否为整数
            int id = Integer.parseInt((String) idObject);
            // 检查是否已经存在相同的用户
            if (userRepository.findById(id) != null) {
                response.put("message", "User already exists");
                return new ResponseEntity<>(response, HttpStatus.CONFLICT); // 409 状态码
            }
            PM_User user = new PM_User();
            user.setId(id);
            user.setPassword((String) requestBody.get("password"));
            PM_User newUser = userRepository.save(user);
            newUser.setPassword("***加密处理***");
            response.put("user", newUser);
            response.put("message", "User created successfully");
            String jwtToken = jwtUtil.generateToken(String.valueOf(id));
            response.put("token", jwtToken);
            return new ResponseEntity<>(response, HttpStatus.CREATED); // 201 状态码
        } catch (NumberFormatException e) {

            response.put("message", "ID is not valid");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // 400 状态码
        } catch (Exception e) {

            return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR); // 500 状态码
        }
    }

    @PostMapping("/admin/user")
    @ApiOperation(value = "管理员添加用户")
    public ResponseEntity<Object> adminAddUser(@RequestBody PM_User user, @RequestHeader("Authorization") String token) {
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
            PM_User admin = userRepository.findById(Integer.parseInt(String.valueOf(id)));
            if (!admin.isRole()) {
                response.put("message", "Access denied");
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN); // 403 状态码
            }

            // 检查是否已经存在相同的用户
            if (userRepository.findById(user.getId()) != null) {
                response.put("message", "User already exists");
                return new ResponseEntity<>(response, HttpStatus.CONFLICT); // 409 状态码
            }
            // 如果密码为空，则设置默认密码
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                user.setPassword("123456");
            }
            PM_User newUser = userRepository.save(user);
            newUser.setPassword("***加密处理***");
            response.put("user", newUser);
            response.put("message", "User created successfully");
            return new ResponseEntity<>(response, HttpStatus.CREATED); // 201 状态码
        } catch (Exception e) {

            return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR); // 500 状态码
        }
    }

    @GetMapping("/user")
    @ApiOperation(value = "获取用户信息")
    public ResponseEntity<Object> getUserInfo(@RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 去掉 Bearer 前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            // 从Token中解析用户id
            String id = jwtUtil.extractUsername(token);
            // 根据id查询数据库中的用户
            PM_User returnUser = userRepository.findById(Integer.parseInt(id));
            returnUser.setPassword("");
            response.put("user", returnUser);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.clear();
            response.put("message", "Unauthorized");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    @PatchMapping("/user")
    @ApiOperation(value = "修改个人信息")
    public ResponseEntity<Object> updateUserInfo(@RequestBody Map<String, PM_User> requestBody, @RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            PM_User user = requestBody.get("user");
            // 去掉 Bearer 前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            // 从Token中解析用户id
            String id = jwtUtil.extractUsername(token);
            // 根据id更新数据库中的用户
            userRepository.updateUserInfoById(Integer.parseInt(id), user.getName(), user.getPhone(), user.getEmail(), user.getAddress());
            response.put("user", user);
            response.put("message", "Updated successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.clear();
            response.put("message", "Invalid format");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // 400 状态码
        }
    }

    @PatchMapping("/user/password")
    @ApiOperation(value = "修改密码")
    public ResponseEntity<Object> updateUserPassword(@RequestBody Map<String, String> requestBody, @RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            String oldPassword = requestBody.get("oldPassword");
            String newPassword = requestBody.get("newPassword");
            // 去掉 Bearer 前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            // 从Token中解析用户id
            String id = jwtUtil.extractUsername(token);
            PM_User returnUser = userRepository.findById(Integer.parseInt(id));
            if(!returnUser.getPassword().equals(oldPassword)){
                response.put("message", "Current password is incorrect");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            } else {
                userRepository.resetPasswordById(Integer.parseInt(id), newPassword);
            }
            response.put("message", "Password updated successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.clear();
            response.put("message", "Invalid format");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // 400 状态码
        }
    }

    @GetMapping ("/admin")
    @ApiOperation(value = "管理员获取用户列表")
    public ResponseEntity<Object> getUserList(@RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 403 拒绝非管理员
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            int adminId = Integer.parseInt(jwtUtil.extractUsername(token));
            PM_User admin = userRepository.findById(adminId);
            if (!admin.isRole()) {
                response.put("message", "Access denied");
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }
            List<PM_User> users = userRepository.findAllByRole(false);

            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception e) {
            // 400
            response.put("message", "Invalid format");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping("/admin/user")
    @ApiOperation(value = "管理员修改用户信息")
    public ResponseEntity<Object> adminUpdateUserInfo(@RequestBody Map<String, PM_User> requestBody, @RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 403 拒绝非管理员
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            int adminId = Integer.parseInt(jwtUtil.extractUsername(token));
            PM_User admin = userRepository.findById(adminId);
            if (!admin.isRole()) {
                response.put("message", "Access denied");
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }
            PM_User user = requestBody.get("user");
            if (userRepository.findById(user.getId()) == null) {
                response.put("message", "User not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            // 根据id更新数据库中的用户
            userRepository.updateUserInfoById(user.getId(), user.getName(), user.getPhone(), user.getEmail(), user.getAddress());
            response.put("message", "User updated successfully");
            response.put("user", user);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            // 400
            response.put("message", "Invalid format");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping("/admin/user/password")
    @ApiOperation(value = "管理员重置密码")
    public ResponseEntity<Object> resetUserPassword(@RequestBody Map<String, Object> requestBody, @RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 403 拒绝非管理员
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            int adminId = Integer.parseInt(jwtUtil.extractUsername(token));
            PM_User admin = userRepository.findById(adminId);
            if (!admin.isRole()) {
                response.put("message", "Access denied");
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }
            // 404
            int id = (int) requestBody.get("id");
            if (userRepository.findById(id) == null) {
                response.put("message", "User not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            String password = (String) requestBody.get("newPassword");
            userRepository.resetPasswordById(id, password);
            // 200
            response.put("message", "Password updated successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (HttpClientErrorException.BadRequest e) {
            // 400
            response.put("message", "Invalid format");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (HttpClientErrorException.Unauthorized e) {
            // 401
            response.put("message", "Unauthorized");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    @DeleteMapping("/admin/user/{userId}")
    @ApiOperation(value = "管理员删除用户")
    public ResponseEntity<Object> deleteUserById(@PathVariable int userId, @RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 403 拒绝非管理员
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            int adminId = Integer.parseInt(jwtUtil.extractUsername(token));
            PM_User admin = userRepository.findById(adminId);
            if (!admin.isRole()) {
                response.put("message", "Access denied");
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }
            // 404
            if (userRepository.findById(userId) == null) {
                response.put("message", "User not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            userRepository.deleteById(userId);
            // 204
            response.put("message", "User deleted successfully");
            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
        } catch (HttpClientErrorException.Unauthorized e) {
            // 401
            response.put("message", "Unauthorized");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }


}
