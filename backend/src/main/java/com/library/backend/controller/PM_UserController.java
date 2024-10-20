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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.library.backend.utils.Constant.*;

@RestController
@Api(tags = "登录验证与用户管理")
@RequestMapping("/api")
public class PM_UserController {

    static int loginUserId;

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
            loginUserId = returnUser.getId();
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

    @GetMapping("/user")
    @ApiOperation(value = "获取用户信息")
    public ResponseEntity<Object> getUsrInfo() {
        Map<String, Object> response = new HashMap<>();
        try {
            PM_User returnUser = userRepository.findById(loginUserId);
            returnUser.setPassword("");
            response.put("usr", returnUser);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.clear();
            response.put("message", "Unauthorized");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    @PatchMapping("/user")
    @ApiOperation(value = "修改个人信息")
    public ResponseEntity<Object> updateUserInfo(@RequestBody Map<String, PM_User> requestBody) {
        Map<String, Object> response = new HashMap<>();
        try {
            PM_User user = requestBody.get("user");
            // 检查是否存在用户
            if (userRepository.findById(user.getId()) == null) {
                response.put("message", "Invalid format"); //
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); //
            }
            userRepository.updateUserInfoById(user.getId(), user.getName(), user.getPhone(), user.getEmail(), user.getAddress());
            response.put("user", user);
            response.put("message", "Updated successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (NumberFormatException e) {
            response.clear();
            response.put("message", "Invalid format");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // 400 状态码
        } catch (Exception e) {
            response.clear();
            response.put("message", "Unauthorized");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/list")
    public Result managerLogin(PM_User user) {
        if (user.getName() != null && !"".equals(user.getName())) {
            List<PM_User> users = userRepository.findAllByNameContaining(user.getName());
            return new Result(SUCCESS_CODE, "", users);
        } else {
            List<PM_User> users = userRepository.findAll();
            return new Result(SUCCESS_CODE, "", users);

        }
    }

    @PostMapping("/add")
    public Result add(@RequestBody PM_User user) {
        try {
            PM_User user1 = userRepository.findByName(user.getName());
            if (user1 != null) {
                return new Result(NAME_REPEAT, "名称重复");
            }
            user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
            userRepository.save(user);
            return new Result(SUCCESS_CODE, "新增成功", user);
        } catch (Exception e) {

            return new Result(FAILE_CODE, e.toString(), user);
        }
    }

    @PostMapping("/update")
    public Result update(@RequestBody PM_User user) {
        try {
            userRepository.save(user);
            return new Result(SUCCESS_CODE, "修改成功", user);
        } catch (Exception e) {
            return new Result(FAILE_CODE, e.toString(), user);
        }
    }

    @DeleteMapping("/delete")
    public Result delete(@RequestBody PM_User user) {
        try {
            userRepository.deleteById(user.getId());
            return new Result(SUCCESS_CODE, "删除成功", user);
        } catch (Exception e) {
            return new Result(FAILE_CODE, e.toString(), user);
        }
    }

}
