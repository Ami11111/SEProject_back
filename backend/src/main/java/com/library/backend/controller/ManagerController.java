package com.library.backend.controller;

import com.library.backend.entity.Manager;
import com.library.backend.entity.User;
import com.library.backend.model.Result;
import com.library.backend.repository.ManagerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.library.backend.utils.Constant.*;

@RestController
@RequestMapping("/manager")
public class ManagerController {

    @Autowired
    private ManagerRepository managerRepository;

    @PostMapping("/login")
    public Result managerLogin(@RequestBody Manager manager) {
        Manager manager1 = managerRepository.findByUsername(manager.getUsername());
        if (manager1 != null && DigestUtils.md5DigestAsHex(manager.getPassword().getBytes()).equals(manager1.getPassword())) {
            return new Result(SUCCESS_CODE, "登录成功");
        } else {
            return new Result(FAILE_CODE, "登录失败");
        }

    }

    @PostMapping("/add")
    public Result add(@RequestBody Manager manager) {
        try {
            Manager user1 = managerRepository.findByUsername(manager.getUsername());
            if (user1 != null) {
                return new Result(NAME_REPEAT, "名称重复");
            }
            manager.setPassword(DigestUtils.md5DigestAsHex(manager.getPassword().getBytes()));
            managerRepository.save(manager);
            return new Result(SUCCESS_CODE, "新增成功", manager);
        } catch (Exception e) {

            return new Result(FAILE_CODE, e.toString(), manager);
        }
    }
}
