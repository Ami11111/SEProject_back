package com.library.backend.controller;

import com.library.backend.entity.PM_User;
import com.library.backend.model.Result;
import com.library.backend.repository.PM_UserRepository;
import com.library.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.library.backend.utils.Constant.*;

@RestController
@RequestMapping("/pm_user")
public class PM_UserController {

    @Autowired
    private PM_UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public Result login(@RequestBody PM_User user) {
        try {
            int cnt = userRepository.countByUsernameAndPassword(user.getUsername(),user.getPassword());
            if (cnt == 0) {
                return new Result(FAILE_CODE, "用户名或密码错误");
            }
            String jwtToken = jwtUtil.generateToken(user.getUsername());
            return new Result(SUCCESS_CODE, "验证成功", userRepository.findByUsername(user.getUsername()));
        } catch (Exception e) {

            return new Result(FAILE_CODE, e.toString(), user);
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
}
