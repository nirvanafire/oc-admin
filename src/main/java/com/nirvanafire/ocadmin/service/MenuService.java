package com.nirvanafire.ocadmin.service;

import com.nirvanafire.ocadmin.dto.MenuDTO;

import java.util.List;

public interface MenuService {
    MenuDTO create(MenuDTO dto);
    MenuDTO update(Long id, MenuDTO dto);
    void delete(Long id);
    MenuDTO getById(Long id);
    List<MenuDTO> getTree();
    List<MenuDTO> getUserMenus(String username);
}
