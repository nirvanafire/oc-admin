package com.nirvanafire.ocadmin.controller;

import com.nirvanafire.ocadmin.dto.LoginRequest;
import com.nirvanafire.ocadmin.dto.RoleDTO;
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
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String adminToken;
    private static Long createdRoleId;

    @BeforeAll
    static void setup(@Autowired MockMvc mockMvc, @Autowired ObjectMapper objectMapper) throws Exception {
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
    @DisplayName("创建角色 - 成功")
    void createRoleSuccess() throws Exception {
        RoleDTO dto = new RoleDTO();
        dto.setCode("test_role");
        dto.setName("测试角色");
        dto.setDescription("这是一个测试角色");
        dto.setEnabled(true);
        dto.setRoleSort(1);

        MvcResult result = mockMvc.perform(post("/api/roles")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.code").value("test_role"))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        createdRoleId = objectMapper.readTree(response).path("data").path("id").asLong();
    }

    @Test
    @Order(2)
    @DisplayName("创建角色 - 编码重复")
    void createRoleDuplicateCode() throws Exception {
        RoleDTO dto = new RoleDTO();
        dto.setCode("test_role");
        dto.setName("重复角色");
        dto.setEnabled(true);

        mockMvc.perform(post("/api/roles")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @Order(3)
    @DisplayName("创建角色 - 编码为空")
    void createRoleEmptyCode() throws Exception {
        RoleDTO dto = new RoleDTO();
        dto.setCode("");
        dto.setName("角色名称");

        mockMvc.perform(post("/api/roles")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    @DisplayName("查询角色列表")
    void getRoleList() throws Exception {
        mockMvc.perform(get("/api/roles")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @Order(5)
    @DisplayName("查询所有角色（无需分页）")
    void getAllRoles() throws Exception {
        mockMvc.perform(get("/api/roles/all")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @Order(6)
    @DisplayName("查询单个角色")
    void getRoleById() throws Exception {
        mockMvc.perform(get("/api/roles/" + createdRoleId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.code").value("test_role"));
    }

    @Test
    @Order(7)
    @DisplayName("查询角色 - 不存在")
    void getRoleNotFound() throws Exception {
        mockMvc.perform(get("/api/roles/99999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @Order(8)
    @DisplayName("更新角色 - 关联菜单")
    void updateRoleWithMenus() throws Exception {
        RoleDTO dto = new RoleDTO();
        dto.setName("测试角色-已更新");
        dto.setDescription("更新后的描述");
        dto.setEnabled(true);
        dto.setMenuIds(Set.of(1L, 2L, 3L));

        mockMvc.perform(put("/api/roles/" + createdRoleId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("测试角色-已更新"));
    }

    @Test
    @Order(9)
    @DisplayName("删除角色 - 成功")
    void deleteRole() throws Exception {
        mockMvc.perform(delete("/api/roles/" + createdRoleId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(10)
    @DisplayName("删除角色 - 已删除的不存在")
    void deleteRoleNotFound() throws Exception {
        mockMvc.perform(delete("/api/roles/" + createdRoleId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }
}
