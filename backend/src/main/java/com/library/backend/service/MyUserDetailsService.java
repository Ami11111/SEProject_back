package com.library.backend.service;

import com.library.backend.entity.PM_Admin;
import com.library.backend.entity.PM_User;
import com.library.backend.repository.PM_AdminRepository;
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

    @Autowired
    private PM_AdminRepository adminRepository;  // 注入管理员仓库

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        PM_User user = userRepository.findById(Integer.parseInt(id));
        PM_Admin admin = adminRepository.findById(Integer.parseInt(id));
        if (user == null && admin == null) {
            throw new UsernameNotFoundException("Id " + id + " 未找到");
        }
        // 如果是用户则返回用户信息，如果是管理员则返回管理员信息
        if (user != null) {
            return new org.springframework.security.core.userdetails.User(
                id,
                user.getPassword(),
                new ArrayList<>()  // 可根据实际情况填充权限
            );
        }
        return new org.springframework.security.core.userdetails.User(
            id,
            admin.getPassword(),
            new ArrayList<>()  // 可根据实际情况填充权限
        );
    }
}
