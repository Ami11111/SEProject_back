package com.library.backend.ServiceTest;

import com.library.backend.service.MyUserDetailsService;
import com.library.backend.repository.PM_AdminRepository;
import com.library.backend.repository.PM_UserRepository;
import com.library.backend.entity.PM_Admin;
import com.library.backend.entity.PM_User;

import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@SpringBootTest
public class MyUserDetailsServiceTest {
    @Autowired
    private MyUserDetailsService myUserDetailsService;
    /*
    @MockBean  //声明被修饰的组件将作为模拟组件，不使用真实组件
    private PM_UserRepository PM_userRepository;

    @MockBean  //声明被修饰的组件将作为模拟组件，不使用真实组件
    private PM_AdminRepository PM_adminRepository;

    @Test
    void loadUserByUsername() {
        PM_User mock_user = new PM_User();
        PM_Admin mock_admin = new PM_Admin();
        mock_user.setId(123);
        mock_user.setPassword("123456");
        mock_admin.setId(456);
        mock_admin.setPassword("654321");
        BDDMockito.given(this.PM_userRepository.findById(123)).willReturn(mock_user);
        BDDMockito.given(this.PM_adminRepository.findById(456)).willReturn(mock_admin);

        BDDMockito.given(this.PM_userRepository.findById(789)).willReturn(null);
        BDDMockito.given(this.PM_adminRepository.findById(789)).willReturn(null);

        myUserDetailsService.loadUserByUsername("123");
        myUserDetailsService.loadUserByUsername("456");

        try{
            myUserDetailsService.loadUserByUsername("789");
        } catch (UsernameNotFoundException e) {
            System.out.print(e);
        }
    }

    */
    @Test
    void loadUserByUsernameWithoutMock(){
        myUserDetailsService.loadUserByUsername("21808081");
        myUserDetailsService.loadUserByUsername("21808080");

        try{
            myUserDetailsService.loadUserByUsername("123");
        } catch (UsernameNotFoundException e) {
            System.out.print(e);
        }
    }
}
