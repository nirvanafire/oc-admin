package com.nirvanafire.ocadmin.controller;

import com.nirvanafire.ocadmin.dto.LoginRequest;
import com.nirvanafire.ocadmin.dto.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String adminToken;
    private static Long createdUserId;

    @BeforeAll
    static void setup(@Autowired MockMvc mockMvc, @Autowired ObjectMapper objectMapper) throws Exception {
        // 管理员登录获取token
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        adminToken = objectMapper.readTree(response).path("data").path("token").asText();
    }

    @Test
    @Order(1)
    @DisplayName("创建用户 - 成功")
    void createUserSuccess() throws Exception {
        UserDTO dto = new UserDTO();
        dto.setUsername("testuser");
        dto.setPassword("Test@123");
        dto.setNickname("测试用户");
        dto.setEmail("test@example.com");
        dto.setPhone("13800138000");
        dto.setEnabled(true);
        dto.setRoleIds(Set.of(2L)); // 普通用户角色

        MvcResult result = mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        createdUserId = objectMapper.readTree(response).path("data").path("id").asLong();
    }

    @Test
    @Order(2)
    @DisplayName("创建用户 - 用户名重复")
    void createUserDuplicateUsername() throws Exception {
        UserDTO dto = new UserDTO();
        dto.setUsername("testuser");
        dto.setPassword("Test@123");
        dto.setEnabled(true);

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @Order(3)
    @DisplayName("创建用户 - 用户名为空")
    void createUserEmptyUsername() throws Exception {
        UserDTO dto = new UserDTO();
        dto.setUsername("");
        dto.setPassword("Test@123");

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    @DisplayName("查询用户列表")
    void getUserList() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @Order(5)
    @DisplayName("查询单个用户")
    void getUserById() throws Exception {
        mockMvc.perform(get("/api/users/" + createdUserId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("testuser"));
    }

    @Test
    @Order(6)
    @DisplayName("查询用户 - 不存在")
    void getUserNotFound() throws Exception {
        mockMvc.perform(get("/api/users/99999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @Order(7)
    @DisplayName("更新用户")
    void updateUser() throws Exception {
        UserDTO dto = new UserDTO();
        dto.setNickname("更新后的昵称");
        dto.setEmail("updated@example.com");
        dto.setEnabled(true);

        mockMvc.perform(put("/api/users/" + createdUserId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.nickname").value("更新后的昵称"));
    }

    @Test
    @Order(8)
    @DisplayName("删除用户 - 成功")
    void deleteUser() throws Exception {
        mockMvc.perform(delete("/api/users/" + createdUserId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(9)
    @DisplayName("无权限访问用户接口")
    void accessWithoutPermission() throws Exception {
        // 使用普通用户token访问（如果有）
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer invalid_token"))
                .andExpect(status().isUnauthorized());
    }
}
