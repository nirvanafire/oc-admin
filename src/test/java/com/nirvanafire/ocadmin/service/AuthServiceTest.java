package com.nirvanafire.ocadmin.service;

import com.nirvanafire.ocadmin.dto.LoginRequest;
import com.nirvanafire.ocadmin.dto.LoginResponse;
import com.nirvanafire.ocadmin.dto.UserDTO;
import com.nirvanafire.ocadmin.entity.SysUser;
import com.nirvanafire.ocadmin.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @Order(1)
    @DisplayName("用户登录 - 成功")
    void loginSuccess() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("admin", response.getUsername());
        assertTrue(response.getRoles().contains("admin"));
    }

    @Test
    @Order(2)
    @DisplayName("用户登录 - 密码错误")
    void loginWrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrongpassword");

        Assertions.assertThrows(Exception.class, () -> {
            authService.login(request);
        });
    }

    @Test
    @Order(3)
    @DisplayName("获取当前用户信息")
    void getCurrentUser() {
        UserDTO user = authService.getCurrentUser("admin");

        assertNotNull(user);
        assertEquals("admin", user.getUsername());
    }

    @Test
    @Order(4)
    @DisplayName("获取当前用户 - 用户不存在")
    void getCurrentUserNotFound() {
        Assertions.assertThrows(Exception.class, () -> {
            authService.getCurrentUser("nonexistent");
        });
    }
}
