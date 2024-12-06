package com.library.backend.config;

import com.library.backend.utils.JwtRequestFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// 测试用的jwtfilter，原因是protected方法无法直接模拟
public class MockJwtRequestFilter extends JwtRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 模拟JWT认证，直接放行请求，不进行验证
        filterChain.doFilter(request, response);
    }

}
