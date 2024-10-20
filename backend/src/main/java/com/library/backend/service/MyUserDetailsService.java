package com.library.backend.service;

import com.library.backend.entity.PM_User;
import com.library.backend.repository.PM_UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private PM_UserRepository userRepository;  // 注入用户仓库

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        PM_User user = userRepository.findById(Integer.parseInt(id));
        if (user == null) {
            throw new UsernameNotFoundException("Id " + id + " 未找到");
        }
        // 返回一个 Spring Security 所需的 UserDetails 对象
        return new org.springframework.security.core.userdetails.User(
            id,
            user.getPassword(),
            new ArrayList<>()  // 可根据实际情况填充权限
        );
    }
}
