package com.library.backend.ServiceTest;

import com.library.backend.service.JwtService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import com.library.backend.entity.PM_Admin;
import com.library.backend.repository.PM_AdminRepository;
import com.library.backend.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PM_AdminRepository adminRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testIsAdmin_ValidAdminToken() {
        // 模拟输入数据
        String token = "Bearer valid.token.string";
        String extractedId = "21808080";
        PM_Admin mockAdmin = new PM_Admin();
        mockAdmin.setId(21808080);
        mockAdmin.setPassword("password");

        Map<String, Object> response = new HashMap<>();

        // 模拟 JwtUtil 和 Repository 的行为
        when(jwtUtil.extractUsername("valid.token.string")).thenReturn(extractedId);
        when(adminRepository.findById(21808080)).thenReturn(mockAdmin);

        // 执行测试
        ResponseEntity<Object> result = jwtService.isAdmin(token, response);

        // 验证
        assertNull(result); // 如果用户是管理员，应该返回 null
        assertTrue(response.isEmpty());

        // 验证调用次数
        verify(jwtUtil, times(1)).extractUsername("valid.token.string");
        verify(adminRepository, times(1)).findById(21808080);
    }

    @Test
    void testIsAdmin_InvalidAdminToken() {
        // 模拟输入数据
        String token = "Bearer invalid.token.string";
        String extractedId = "21808081"; // 提取到一个不存在的管理员 ID
        Map<String, Object> response = new HashMap<>();

        // 模拟 JwtUtil 和 Repository 的行为
        when(jwtUtil.extractUsername("invalid.token.string")).thenReturn(extractedId);
        when(adminRepository.findById(21808081)).thenReturn(null);

        // 执行测试
        ResponseEntity<Object> result = jwtService.isAdmin(token, response);

        // 验证
        assertNotNull(result); // 非管理员用户应该返回响应实体
        assertEquals(401, result.getStatusCodeValue());
        assertEquals("Access denied", response.get("message"));

        // 验证调用次数
        verify(jwtUtil, times(1)).extractUsername("invalid.token.string");
        verify(adminRepository, times(1)).findById(21808081);
    }

    @Test
    void testIsAdmin_TokenWithoutBearerPrefix() {
        // 模拟输入数据
        String token = "valid.token.string"; // 缺少 Bearer 前缀
        String extractedId = "21808080";
        PM_Admin mockAdmin = new PM_Admin();
        mockAdmin.setId(21808080);
        mockAdmin.setPassword("password");

        Map<String, Object> response = new HashMap<>();

        // 模拟 JwtUtil 和 Repository 的行为
        when(jwtUtil.extractUsername(token)).thenReturn(extractedId);
        when(adminRepository.findById(21808080)).thenReturn(mockAdmin);

        // 执行测试
        ResponseEntity<Object> result = jwtService.isAdmin(token, response);

        // 验证
        assertNull(result); // 如果用户是管理员，应该返回 null
        assertTrue(response.isEmpty());

        // 验证调用次数
        verify(jwtUtil, times(1)).extractUsername(token);
        verify(adminRepository, times(1)).findById(21808080);
    }
}
