package com.library.backend.controller;

import com.library.backend.entity.Book;
import com.library.backend.entity.Manager;
import com.library.backend.entity.User;
import com.library.backend.model.Result;
import com.library.backend.repository.BookRepository;
import com.library.backend.repository.ManagerRepository;
import com.library.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.library.backend.utils.Constant.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/list")
    public Result managerLogin(User user) {
        if (user.getUsername() != null && !"".equals(user.getUsername())) {
            List<User> users = userRepository.findAllByUsernameContaining(user.getUsername());
            return new Result(SUCCESS_CODE, "", users);
        } else {
            List<User> users = userRepository.findAll();
            return new Result(SUCCESS_CODE, "", users);

        }
    }

    @PostMapping("/add")
    public Result add(@RequestBody User user) {
        try {
            User user1 = userRepository.findByUsername(user.getUsername());
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
    public Result update(@RequestBody User user) {
        try {
            userRepository.save(user);
            return new Result(SUCCESS_CODE, "修改成功", user);
        } catch (Exception e) {
            return new Result(FAILE_CODE, e.toString(), user);
        }
    }

    @DeleteMapping("/delete")
    public Result delete(@RequestBody User user) {
        try {
            userRepository.deleteById(user.getId());
            return new Result(SUCCESS_CODE, "删除成功", user);
        } catch (Exception e) {
            return new Result(FAILE_CODE, e.toString(), user);
        }
    }
}
