package com.nirvanafire.ocadmin.service;

import com.nirvanafire.ocadmin.dto.RoleDTO;
import com.nirvanafire.ocadmin.common.exception.BusinessException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RoleServiceTest {

    @Autowired
    private RoleService roleService;

    private static Long createdRoleId;

    @Test
    @Order(1)
    @DisplayName("创建角色 - 成功")
    void createRoleSuccess() {
        RoleDTO dto = new RoleDTO();
        dto.setCode("tester");
        dto.setName("测试员");
        dto.setDescription("测试角色");
        dto.setEnabled(true);
        dto.setRoleSort(10);

        RoleDTO result = roleService.create(dto);

        assertNotNull(result);
        assertEquals("tester", result.getCode());
        assertEquals("测试员", result.getName());
        createdRoleId = result.getId();
    }

    @Test
    @Order(2)
    @DisplayName("创建角色 - 编码重复")
    void createRoleDuplicateCode() {
        RoleDTO dto = new RoleDTO();
        dto.setCode("tester");
        dto.setName("重复角色");

        assertThrows(BusinessException.class, () -> {
            roleService.create(dto);
        });
    }

    @Test
    @Order(3)
    @DisplayName("查询角色列表")
    void listRoles() {
        Page<RoleDTO> page = roleService.list(org.springframework.data.domain.PageRequest.of(0, 10));

        assertNotNull(page);
        assertTrue(page.getTotalElements() > 0);
    }

    @Test
    @Order(4)
    @DisplayName("查询所有角色")
    void getAllRoles() {
        List<RoleDTO> roles = roleService.all();

        assertNotNull(roles);
        assertTrue(roles.size() >= 2); // admin + user
    }

    @Test
    @Order(5)
    @DisplayName("根据ID查询角色")
    void getRoleById() {
        RoleDTO role = roleService.getById(createdRoleId);

        assertNotNull(role);
        assertEquals("tester", role.getCode());
    }

    @Test
    @Order(6)
    @DisplayName("更新角色")
    void updateRole() {
        RoleDTO dto = new RoleDTO();
        dto.setName("测试员-已更新");
        dto.setDescription("更新后的描述");

        RoleDTO result = roleService.update(createdRoleId, dto);

        assertNotNull(result);
        assertEquals("测试员-已更新", result.getName());
    }

    @Test
    @Order(7)
    @DisplayName("删除角色")
    void deleteRole() {
        assertDoesNotThrow(() -> {
            roleService.delete(createdRoleId);
        });
    }

    @Test
    @Order(8)
    @DisplayName("删除不存在的角色")
    void deleteNonExistentRole() {
        assertThrows(BusinessException.class, () -> {
            roleService.delete(99999L);
        });
    }
}
