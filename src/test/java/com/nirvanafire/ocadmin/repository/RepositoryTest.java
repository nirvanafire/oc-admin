package com.nirvanafire.ocadmin.repository;

import com.nirvanafire.ocadmin.entity.SysRole;
import com.nirvanafire.ocadmin.entity.SysUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class RepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Test
    void testUserRepositoryFindByUsername() {
        Optional<SysUser> user = userRepository.findByUsername("admin");
        assertTrue(user.isPresent());
        assertEquals("admin", user.get().getUsername());
    }

    @Test
    void testUserRepositoryExistsByUsername() {
        assertTrue(userRepository.existsByUsername("admin"));
        assertFalse(userRepository.existsByUsername("nonexistent"));
    }

    @Test
    void testUserRepositoryFindByUsernameNotFound() {
        Optional<SysUser> user = userRepository.findByUsername("nonexistent");
        assertTrue(user.isEmpty());
    }

    @Test
    void testRoleRepositoryFindByCode() {
        Optional<SysRole> role = roleRepository.findByCode("admin");
        assertTrue(role.isPresent());
        assertEquals("超级管理员", role.get().getName());
    }

    @Test
    void testRoleRepositoryExistsByCode() {
        assertTrue(roleRepository.existsByCode("admin"));
        assertFalse(roleRepository.existsByCode("nonexistent"));
    }

    @Test
    void testMenuRepositoryFindByParentId() {
        var menus = menuRepository.findByParentIdOrderByMenuSort(0L);
        assertNotNull(menus);
        // 应该至少包含系统管理菜单
        assertTrue(menus.size() >= 1);
    }

    @Test
    void testMenuRepositoryFindByVisible() {
        var menus = menuRepository.findByVisibleOrderByMenuSort("1");
        assertNotNull(menus);
        assertTrue(menus.size() > 0);
    }
}
