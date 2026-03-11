package com.nirvanafire.ocadmin.service.impl;

import com.nirvanafire.ocadmin.common.exception.BusinessException;
import com.nirvanafire.ocadmin.dto.MenuDTO;
import com.nirvanafire.ocadmin.entity.SysMenu;
import com.nirvanafire.ocadmin.entity.SysUser;
import com.nirvanafire.ocadmin.repository.MenuRepository;
import com.nirvanafire.ocadmin.repository.UserRepository;
import com.nirvanafire.ocadmin.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public MenuDTO create(MenuDTO dto) {
        SysMenu menu = SysMenu.builder()
                .name(dto.getName())
                .path(dto.getPath())
                .component(dto.getComponent())
                .menuType(dto.getMenuType() != null ? dto.getMenuType() : "menu")
                .icon(dto.getIcon())
                .parentId(dto.getParentId() != null ? dto.getParentId() : 0L)
                .menuSort(dto.getMenuSort() != null ? dto.getMenuSort() : 0)
                .visible(dto.getVisible() != null ? dto.getVisible() : "1")
                .keepAlive(dto.getKeepAlive() != null ? dto.getKeepAlive() : true)
                .alwaysShow(dto.getAlwaysShow() != null ? dto.getAlwaysShow() : false)
                .remark(dto.getRemark())
                .build();
        
        menu = menuRepository.save(menu);
        return toDTO(menu);
    }

    @Override
    @Transactional
    public MenuDTO update(Long id, MenuDTO dto) {
        SysMenu menu = menuRepository.findById(id)
                .orElseThrow(() -> new BusinessException("菜单不存在"));
        
        menu.setName(dto.getName());
        menu.setPath(dto.getPath());
        menu.setComponent(dto.getComponent());
        menu.setMenuType(dto.getMenuType());
        menu.setIcon(dto.getIcon());
        menu.setParentId(dto.getParentId());
        menu.setMenuSort(dto.getMenuSort());
        menu.setVisible(dto.getVisible());
        menu.setKeepAlive(dto.getKeepAlive());
        menu.setAlwaysShow(dto.getAlwaysShow());
        menu.setRemark(dto.getRemark());
        
        menu = menuRepository.save(menu);
        return toDTO(menu);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!menuRepository.existsById(id)) {
            throw new BusinessException("菜单不存在");
        }
        // 检查是否有子菜单
        List<SysMenu> children = menuRepository.findByParentIdOrderByMenuSort(id);
        if (!children.isEmpty()) {
            throw new BusinessException("存在子菜单，无法删除");
        }
        menuRepository.deleteById(id);
    }

    @Override
    public MenuDTO getById(Long id) {
        SysMenu menu = menuRepository.findById(id)
                .orElseThrow(() -> new BusinessException("菜单不存在"));
        return toDTO(menu);
    }

    @Override
    public List<MenuDTO> getTree() {
        List<SysMenu> allMenus = menuRepository.findByVisibleOrderByMenuSort("1");
        return buildTree(allMenus, 0L);
    }

    @Override
    public List<MenuDTO> getUserMenus(String username) {
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        // 获取用户所有角色关联的菜单
        Set<SysMenu> menus = user.getRoles().stream()
                .flatMap(role -> role.getMenus().stream())
                .collect(Collectors.toSet());
        
        return buildTree(new ArrayList<>(menus), 0L);
    }

    private List<MenuDTO> buildTree(List<SysMenu> menus, Long parentId) {
        return menus.stream()
                .filter(menu -> parentId.equals(menu.getParentId()))
                .map(menu -> {
                    MenuDTO dto = toDTO(menu);
                    dto.setChildren(buildTree(menus, menu.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private MenuDTO toDTO(SysMenu menu) {
        return MenuDTO.builder()
                .id(menu.getId())
                .name(menu.getName())
                .path(menu.getPath())
                .component(menu.getComponent())
                .menuType(menu.getMenuType())
                .icon(menu.getIcon())
                .parentId(menu.getParentId())
                .menuSort(menu.getMenuSort())
                .visible(menu.getVisible())
                .keepAlive(menu.getKeepAlive())
                .alwaysShow(menu.getAlwaysShow())
                .remark(menu.getRemark())
                .build();
    }
}
