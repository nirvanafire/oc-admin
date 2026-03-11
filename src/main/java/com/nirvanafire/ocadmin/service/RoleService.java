package com.nirvanafire.ocadmin.service;

import com.nirvanafire.ocadmin.dto.MenuDTO;
import com.nirvanafire.ocadmin.dto.RoleDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RoleService {
    RoleDTO create(RoleDTO dto);
    RoleDTO update(Long id, RoleDTO dto);
    void delete(Long id);
    RoleDTO getById(Long id);
    Page<RoleDTO> list(Pageable pageable);
    List<RoleDTO> all();
}

public interface MenuService {
    MenuDTO create(MenuDTO dto);
    MenuDTO update(Long id, MenuDTO dto);
    void delete(Long id);
    MenuDTO getById(Long id);
    List<MenuDTO> getTree();
    List<MenuDTO> getUserMenus(String username);
}
