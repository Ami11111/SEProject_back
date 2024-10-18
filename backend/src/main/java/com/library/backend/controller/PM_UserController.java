package com.library.backend.controller;

import com.library.backend.entity.PM_User;
import com.library.backend.entity.User;
import com.library.backend.model.Result;
import com.library.backend.repository.PM_UserRepository;
import com.library.backend.repository.UserRepository;
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

    @GetMapping("/list")
    public Result managerLogin(PM_User user) {
        if (user.getName() != null && !"".equals(user.getName())) {
            List<PM_User> users = userRepository.findAllByUsernameContaining(user.getName());
            return new Result(SUCCESS_CODE, "", users);
        } else {
            List<PM_User> users = userRepository.findAll();
            return new Result(SUCCESS_CODE, "", users);

        }
    }

    @PostMapping("/add")
    public Result add(@RequestBody PM_User user) {
        try {
            PM_User user1 = userRepository.findByUsername(user.getName());
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
