package com.library.backend.DaoTest;

import com.library.backend.entity.PM_User;
import com.library.backend.repository.PM_UserRepository;
import com.library.backend.utils.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
public class PM_UserRepositoryTest {
    @Autowired
    private PM_UserRepository userRepository;

    @Test
    void add(){
        PM_User newUser = new PM_User();
        newUser.setId(12345);
        newUser.setName("test_add");
        newUser.setPassword("12345");
        userRepository.save(newUser);
        int count = userRepository.countById(12345);
        assert count == 1;
    }

    @Test
    void countByIdAndPassword(){
        PM_User newUser = new PM_User();
        newUser.setId(12345);
        newUser.setPassword("12345");
        userRepository.save(newUser);
        int count = userRepository.countByIdAndPassword(12345, "12345");
        assert count == 1;
        userRepository.deleteById(12345);
        count = userRepository.countByIdAndPassword(12345, "12345");
        assert count == 0;
    }

    @Test
    void countById(){
        PM_User newUser = new PM_User();
        newUser.setId(12345);
        newUser.setPassword("12345");
        userRepository.save(newUser);
        int count = userRepository.countById(12345);
        assert count == 1;
        userRepository.deleteById(12345);
        count = userRepository.countById(12345);
        assert count == 0;
    }

    @Test
    void findById(){
        PM_User newUser = new PM_User();
        newUser.setId(12345);
        newUser.setPassword("12345");
        userRepository.save(newUser);
        PM_User returnUser = userRepository.findById(12345);
        assert returnUser.getId() == 12345;
        assert returnUser.getPassword().equals("12345");
    }

    @Test
    void deleteById(){
        PM_User newUser = new PM_User();
        newUser.setId(12345);
        newUser.setPassword("12345");
        userRepository.save(newUser);
        int count = userRepository.countById(12345);
        assert count == 1;
        userRepository.deleteById(12345);
        count = userRepository.countById(12345);
        assert count == 0;
    }

    @Test
    void updateUserInfoById(){
        PM_User newUser = new PM_User();
        newUser.setId(12345);
        newUser.setPassword("12345");
        userRepository.save(newUser);
        PM_User returnUser = userRepository.findById(12345);
        assert returnUser.getId() == 12345;
        assert returnUser.getPassword().equals("12345");
        userRepository.updateUserInfoById(12345, "new_name", "110", "tmp@gmail.com", "some_where");
        returnUser = userRepository.findById(12345);
        assert returnUser.getId() == 12345;
        assert returnUser.getName().equals("new_name");
        assert returnUser.getPhone().equals("110");
        assert returnUser.getEmail().equals("tmp@gmail.com");
        assert returnUser.getAddress().equals("some_where");
    }

    @Test
    void resetPasswordById(){
        PM_User newUser = new PM_User();
        newUser.setId(12345);
        newUser.setPassword("12345");
        userRepository.save(newUser);
        PM_User returnUser = userRepository.findById(12345);
        assert returnUser.getId() == 12345;
        assert returnUser.getPassword().equals("12345");
        userRepository.resetPasswordById(12345, "123456");
        returnUser = userRepository.findById(12345);
        assert returnUser.getId() == 12345;
        assert returnUser.getPassword().equals("123456");
    }
}
