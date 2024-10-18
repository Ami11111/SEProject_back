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

    static PM_User loginUsr;

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
            int cnt = userRepository.countByUsernameAndPassword(user.getUsername(), user.getPassword());
            if (cnt == 0) {
                response.put("message", "用户名或密码错误");
                // 返回一个 ResponseEntity 对象，包含响应体和状态码
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED); // 401 状态码
            }
            // 根据用户名生成 JWT Token， 只在登录成功时生成，后续操作由JWT过滤器自动校验前端发来的Token
            String jwtToken = jwtUtil.generateToken(user.getUsername());
            response.put("token", jwtToken);
            response.put("user", userRepository.findByUsername(user.getUsername()));
            loginUsr = userRepository.findByUsername(user.getUsername());
            // 返回一个 ResponseEntity 对象，包含响应体和状态码
            return new ResponseEntity<>(response, HttpStatus.OK); // 200 状态码
        } catch (Exception e) {

            return new ResponseEntity<>(e.toString(), HttpStatus.INTERNAL_SERVER_ERROR); // 500 状态码
        }
    }

    @GetMapping("/list")
    public Result managerLogin(PM_User user) {
        if (user.getUsername() != null && !"".equals(user.getUsername())) {
            List<PM_User> users = userRepository.findAllByUsernameContaining(user.getUsername());
            return new Result(SUCCESS_CODE, "", users);
        } else {
            List<PM_User> users = userRepository.findAll();
            return new Result(SUCCESS_CODE, "", users);

        }
    }

    @PostMapping("/add")
    public Result add(@RequestBody PM_User user) {
        try {
            PM_User user1 = userRepository.findByUsername(user.getUsername());
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

    @GetMapping("/usr")
    public ResponseEntity<Object> getUsrInfo() {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("id", loginUsr.getId());
            response.put("username", loginUsr.getUsername());
            response.put("phone", loginUsr.getPhone());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.clear();
            response.put("message", "Unauthorized");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    @PatchMapping ("/usr/username")
    public ResponseEntity<Object> updateNewUserName(PM_User usr) {
        Map<String, Object> response = new HashMap<>();
        try {
            userRepository.updateUsernameById(loginUsr.getId(), usr.getUsername());
            response.put("username", usr.getUsername());
            response.put("message", "Username updated successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.clear();
            response.put("message", "Invalid format");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
}
