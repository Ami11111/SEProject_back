package com.library.backend.config;

import com.library.backend.service.MyUserDetailsService;
import com.library.backend.utils.JwtRequestFilter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@TestConfiguration
@EnableWebSecurity
@Order(200)
public class TestSecurityConfig extends WebSecurityConfigurerAdapter {

    // JwtFilter用
    @Bean
    public MyUserDetailsService myUserDetailsService() {
        return new MyUserDetailsService();
    }

    // 用模拟的JwtFilter取代真实的
    @Bean(name = "testJwtRequestFilter")
    @Primary
    public JwtRequestFilter jwtRequestFilter() {
        return new MockJwtRequestFilter();
    }

    // 测试用，跳过jwt过滤器
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 禁用 CSRF（跨站请求伪造。测试中通常不使用 CSRF 保护）
        http.csrf().disable()
                .authorizeRequests()
                .anyRequest().permitAll()
                .and()
                .formLogin().disable()  // 禁用表单登录
                .httpBasic().disable()  // 禁用基本认证
                .addFilterBefore(jwtRequestFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/api/**");
    }
}


