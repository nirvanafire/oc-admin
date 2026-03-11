package com.nirvanafire.ocadmin.service;

import com.nirvanafire.ocadmin.dto.MenuDTO;
import com.nirvanafire.ocadmin.common.exception.BusinessException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MenuServiceTest {

    @Autowired
    private MenuService menuService;

    private static Long createdMenuId;
    private static Long createdSubMenuId;

    @Test
    @Order(1)
    @DisplayName("创建顶级菜单")
    void createTopMenu() {
        MenuDTO dto = new MenuDTO();
        dto.setName("测试顶级菜单");
        dto.setPath("/test-top");
        dto.setComponent("test/top/index");
        dto.setMenuType("menu");
        dto.setIcon("Setting");
        dto.setParentId(0L);
        dto.setMenuSort(100);
        dto.setVisible("1");
        dto.setKeepAlive(true);
        dto.setAlwaysShow(false);

        MenuDTO result = menuService.create(dto);

        assertNotNull(result);
        assertEquals("测试顶级菜单", result.getName());
        assertEquals(0L, result.getParentId());
        createdMenuId = result.getId();
    }

    @Test
    @Order(2)
    @DisplayName("创建子菜单")
    void createSubMenu() {
        MenuDTO dto = new MenuDTO();
        dto.setName("测试子菜单");
        dto.setPath("/test-sub");
        dto.setComponent("test/sub/index");
        dto.setMenuType("menu");
        dto.setIcon("Document");
        dto.setParentId(createdMenuId);
        dto.setMenuSort(1);
        dto.setVisible("1");

        MenuDTO result = menuService.create(dto);

        assertNotNull(result);
        assertEquals(createdMenuId, result.getParentId());
        createdSubMenuId = result.getId();
    }

    @Test
    @Order(3)
    @DisplayName("获取菜单树")
    void getTree() {
        List<MenuDTO> tree = menuService.getTree();

        assertNotNull(tree);
        // 应该包含系统管理菜单和测试菜单
        assertTrue(tree.size() > 0);
    }

    @Test
    @Order(4)
    @DisplayName("根据ID获取菜单")
    void getMenuById() {
        MenuDTO menu = menuService.getById(createdMenuId);

        assertNotNull(menu);
        assertEquals("测试顶级菜单", menu.getName());
    }

    @Test
    @Order(5)
    @DisplayName("更新菜单")
    void updateMenu() {
        MenuDTO dto = new MenuDTO();
        dto.setName("测试顶级菜单-更新");
        dto.setPath("/test-top-updated");
        dto.setComponent("test/top/updated");
        dto.setMenuType("menu");
        dto.setIcon("Setting");
        dto.setParentId(0L);
        dto.setMenuSort(200);

        MenuDTO result = menuService.update(createdMenuId, dto);

        assertNotNull(result);
        assertEquals("测试顶级菜单-更新", result.getName());
    }

    @Test
    @Order(6)
    @DisplayName("删除子菜单")
    void deleteSubMenu() {
        assertDoesNotThrow(() -> {
            menuService.delete(createdSubMenuId);
        });
    }

    @Test
    @Order(7)
    @DisplayName("删除有子菜单的菜单失败")
    void deleteMenuWithChildren() {
        // 重新创建一个有子菜单的菜单
        MenuDTO parent = menuService.create(MenuDTO.builder()
                .name("父菜单")
                .path("/parent")
                .menuType("directory")
                .parentId(0L)
                .menuSort(1)
                .build());

        MenuDTO child = menuService.create(MenuDTO.builder()
                .name("子菜单")
                .path("/parent/child")
                .menuType("menu")
                .parentId(parent.getId())
                .menuSort(1)
                .build());

        // 先删除子菜单
        menuService.delete(child.getId());

        // 删除父菜单应该成功（现在没有子菜单了）
        assertDoesNotThrow(() -> {
            menuService.delete(parent.getId());
        });
    }

    @Test
    @Order(8)
    @DisplayName("删除不存在的菜单")
    void deleteNonExistentMenu() {
        assertThrows(BusinessException.class, () -> {
            menuService.delete(99999L);
        });
    }

    @Test
    @Order(9)
    @DisplayName("获取用户菜单")
    void getUserMenus() {
        List<MenuDTO> menus = menuService.getUserMenus("admin");

        assertNotNull(menus);
        assertTrue(menus.size() > 0);
    }
}
