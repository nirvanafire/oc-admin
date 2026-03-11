package com.nirvanafire.ocadmin.service;

import com.nirvanafire.ocadmin.dto.UserDTO;
import com.nirvanafire.ocadmin.common.exception.BusinessException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceTest {

    @Autowired
    private UserService userService;

    private static Long createdUserId;

    @Test
    @Order(1)
    @DisplayName("创建用户 - 成功")
    void createUserSuccess() {
        UserDTO dto = new UserDTO();
        dto.setUsername("newuser");
        dto.setPassword("Pass@123");
        dto.setNickname("新用户");
        dto.setEmail("newuser@test.com");
        dto.setPhone("13900139000");
        dto.setEnabled(true);
        dto.setRoleIds(Set.of(2L));

        UserDTO result = userService.create(dto);

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("新用户", result.getNickname());
        createdUserId = result.getId();
    }

    @Test
    @Order(2)
    @DisplayName("创建用户 - 用户名重复")
    void createUserDuplicateUsername() {
        UserDTO dto = new UserDTO();
        dto.setUsername("newuser");
        dto.setPassword("Pass@123");

        assertThrows(BusinessException.class, () -> {
            userService.create(dto);
        });
    }

    @Test
    @Order(3)
    @DisplayName("查询用户列表")
    void listUsers() {
        Page<UserDTO> page = userService.list(org.springframework.data.domain.PageRequest.of(0, 10));

        assertNotNull(page);
        assertTrue(page.getTotalElements() > 0);
    }

    @Test
    @Order(4)
    @DisplayName("根据ID查询用户")
    void getUserById() {
        UserDTO user = userService.getById(createdUserId);

        assertNotNull(user);
        assertEquals("newuser", user.getUsername());
    }

    @Test
    @Order(5)
    @DisplayName("更新用户")
    void updateUser() {
        UserDTO dto = new UserDTO();
        dto.setNickname("更新的昵称");
        dto.setEmail("updated@test.com");

        UserDTO result = userService.update(createdUserId, dto);

        assertNotNull(result);
        assertEquals("更新的昵称", result.getNickname());
    }

    @Test
    @Order(6)
    @DisplayName("删除用户")
    void deleteUser() {
        assertDoesNotThrow(() -> {
            userService.delete(createdUserId);
        });
    }

    @Test
    @Order(7)
    @DisplayName("删除不存在的用户")
    void deleteNonExistentUser() {
        assertThrows(BusinessException.class, () -> {
            userService.delete(99999L);
        });
    }
}
