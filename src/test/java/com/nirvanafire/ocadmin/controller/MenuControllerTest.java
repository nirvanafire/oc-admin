package com.nirvanafire.ocadmin.controller;

import com.nirvanafire.ocadmin.dto.LoginRequest;
import com.nirvanafire.ocadmin.dto.MenuDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String adminToken;
    private static Long createdMenuId;
    private static Long createdSubMenuId;

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
    @DisplayName("查询菜单树")
    void getMenuTree() throws Exception {
        mockMvc.perform(get("/api/menus/tree")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @Order(2)
    @DisplayName("创建目录 - 成功")
    void createDirectory() throws Exception {
        MenuDTO dto = new MenuDTO();
        dto.setName("测试目录");
        dto.setPath("/test");
        dto.setMenuType("directory");
        dto.setIcon("Setting");
        dto.setParentId(0L);
        dto.setMenuSort(100);
        dto.setVisible("1");

        MvcResult result = mockMvc.perform(post("/api/menus")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.menuType").value("directory"))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        createdMenuId = objectMapper.readTree(response).path("data").path("id").asLong();
    }

    @Test
    @Order(3)
    @DisplayName("创建子菜单")
    void createSubMenu() throws Exception {
        MenuDTO dto = new MenuDTO();
        dto.setName("测试菜单");
        dto.setPath("/test/menu");
        dto.setComponent("test/menu/index");
        dto.setMenuType("menu");
        dto.setIcon("Document");
        dto.setParentId(createdMenuId);
        dto.setMenuSort(1);
        dto.setVisible("1");

        MvcResult result = mockMvc.perform(post("/api/menus")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.parentId").value(createdMenuId))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        createdSubMenuId = objectMapper.readTree(response).path("data").path("id").asLong();
    }

    @Test
    @Order(4)
    @DisplayName("创建菜单 - 名称为空")
    void createMenuEmptyName() throws Exception {
        MenuDTO dto = new MenuDTO();
        dto.setName("");
        dto.setPath("/test");
        dto.setMenuType("menu");

        mockMvc.perform(post("/api/menus")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(5)
    @DisplayName("查询单个菜单")
    void getMenuById() throws Exception {
        mockMvc.perform(get("/api/menus/" + createdMenuId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("测试目录"));
    }

    @Test
    @Order(6)
    @DisplayName("更新菜单")
    void updateMenu() throws Exception {
        MenuDTO dto = new MenuDTO();
        dto.setName("测试目录-已更新");
        dto.setPath("/test-updated");
        dto.setMenuType("directory");
        dto.setIcon("Setting");
        dto.setParentId(0L);
        dto.setMenuSort(200);

        mockMvc.perform(put("/api/menus/" + createdMenuId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("测试目录-已更新"));
    }

    @Test
    @Order(7)
    @DisplayName("删除菜单 - 有子菜单失败")
    void deleteMenuWithChildren() throws Exception {
        mockMvc.perform(delete("/api/menus/" + createdMenuId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500)); // 存在子菜单不能删除
    }

    @Test
    @Order(8)
    @DisplayName("先删除子菜单")
    void deleteSubMenu() throws Exception {
        mockMvc.perform(delete("/api/menus/" + createdSubMenuId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(9)
    @DisplayName("再删除父菜单")
    void deleteParentMenu() throws Exception {
        mockMvc.perform(delete("/api/menus/" + createdMenuId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @Order(10)
    @DisplayName("查询用户菜单")
    void getUserMenus() throws Exception {
        mockMvc.perform(get("/api/menus/user")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }
}
