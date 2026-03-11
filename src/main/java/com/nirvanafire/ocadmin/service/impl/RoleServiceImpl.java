package com.nirvanafire.ocadmin.service.impl;

import com.nirvanafire.ocadmin.common.exception.BusinessException;
import com.nirvanafire.ocadmin.dto.RoleDTO;
import com.nirvanafire.ocadmin.entity.SysMenu;
import com.nirvanafire.ocadmin.entity.SysPermission;
import com.nirvanafire.ocadmin.entity.SysRole;
import com.nirvanafire.ocadmin.repository.MenuRepository;
import com.nirvanafire.ocadmin.repository.PermissionRepository;
import com.nirvanafire.ocadmin.repository.RoleRepository;
import com.nirvanafire.ocadmin.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final MenuRepository menuRepository;

    @Override
    @Transactional
    public RoleDTO create(RoleDTO dto) {
        if (roleRepository.existsByCode(dto.getCode())) {
            throw new BusinessException("角色编码已存在");
        }
        
        SysRole role = SysRole.builder()
                .code(dto.getCode())
                .name(dto.getName())
                .description(dto.getDescription())
                .roleSort(dto.getRoleSort() != null ? dto.getRoleSort() : 0)
                .enabled(dto.getEnabled() != null ? dto.getEnabled() : true)
                .build();
        
        setPermissionsAndMenus(role, dto);
        
        role = roleRepository.save(role);
        return toDTO(role);
    }

    @Override
    @Transactional
    public RoleDTO update(Long id, RoleDTO dto) {
        SysRole role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("角色不存在"));
        
        role.setName(dto.getName());
        role.setDescription(dto.getDescription());
        if (dto.getRoleSort() != null) {
            role.setRoleSort(dto.getRoleSort());
        }
        if (dto.getEnabled() != null) {
            role.setEnabled(dto.getEnabled());
        }
        
        setPermissionsAndMenus(role, dto);
        
        role = roleRepository.save(role);
        return toDTO(role);
    }

    private void setPermissionsAndMenus(SysRole role, RoleDTO dto) {
        if (dto.getPermissionIds() != null) {
            Set<SysPermission> permissions = dto.getPermissionIds().stream()
                    .map(id -> permissionRepository.findById(id)
                            .orElseThrow(() -> new BusinessException("权限不存在: " + id)))
                    .collect(Collectors.toSet());
            role.setPermissions(permissions);
        }
        
        if (dto.getMenuIds() != null) {
            Set<SysMenu> menus = dto.getMenuIds().stream()
                    .map(id -> menuRepository.findById(id)
                            .orElseThrow(() -> new BusinessException("菜单不存在: " + id)))
                    .collect(Collectors.toSet());
            role.setMenus(menus);
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new BusinessException("角色不存在");
        }
        roleRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDTO getById(Long id) {
        SysRole role = roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("角色不存在"));
        return toDTO(role);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoleDTO> list(Pageable pageable) {
        Page<SysRole> page = roleRepository.findAll(pageable);
        List<RoleDTO> list = page.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(list, pageable, page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoleDTO> list(Pageable pageable, String code) {
        Page<SysRole> page;
        if (StringUtils.hasText(code)) {
            page = roleRepository.findByCodeContaining(code, pageable);
        } else {
            page = roleRepository.findAll(pageable);
        }
        List<RoleDTO> list = page.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(list, pageable, page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleDTO> all() {
        return roleRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private RoleDTO toDTO(SysRole role) {
        return RoleDTO.builder()
                .id(role.getId())
                .code(role.getCode())
                .name(role.getName())
                .description(role.getDescription())
                .roleSort(role.getRoleSort())
                .enabled(role.getEnabled())
                .permissionIds(role.getPermissions().stream().map(SysPermission::getId).collect(Collectors.toSet()))
                .menuIds(role.getMenus().stream().map(SysMenu::getId).collect(Collectors.toSet()))
                .build();
    }
}
