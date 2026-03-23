package com.nirvanafire.ocadmin.service.impl;

import com.nirvanafire.ocadmin.common.exception.BusinessException;
import com.nirvanafire.ocadmin.dto.PermissionDTO;
import com.nirvanafire.ocadmin.dto.PermissionRequest;
import com.nirvanafire.ocadmin.entity.SysPermission;
import com.nirvanafire.ocadmin.repository.PermissionRepository;
import com.nirvanafire.ocadmin.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;

    @Override
    @Transactional
    public PermissionDTO create(PermissionRequest request) {
        if (permissionRepository.existsByCode(request.getCode())) {
            throw new BusinessException("权限编码已存在");
        }

        SysPermission permission = SysPermission.builder()
                .code(request.getCode())
                .name(request.getName())
                .category(request.getCategory())
                .permissionType(request.getPermissionType() != null ? request.getPermissionType() : "button")
                .description(request.getDescription())
                .permissionSort(request.getPermissionSort() != null ? request.getPermissionSort() : 0)
                .build();

        permission = permissionRepository.save(permission);
        return toDTO(permission);
    }

    @Override
    @Transactional
    public PermissionDTO update(Long id, PermissionRequest request) {
        SysPermission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("权限不存在"));

        // 如果修改了编码，检查是否与其它权限冲突
        if (!permission.getCode().equals(request.getCode()) &&
                permissionRepository.existsByCode(request.getCode())) {
            throw new BusinessException("权限编码已存在");
        }

        permission.setCode(request.getCode());
        permission.setName(request.getName());
        permission.setCategory(request.getCategory());
        permission.setPermissionType(request.getPermissionType());
        permission.setDescription(request.getDescription());
        permission.setPermissionSort(request.getPermissionSort() != null ? request.getPermissionSort() : 0);

        permission = permissionRepository.save(permission);
        return toDTO(permission);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!permissionRepository.existsById(id)) {
            throw new BusinessException("权限不存在");
        }
        permissionRepository.deleteById(id);
    }

    @Override
    public PermissionDTO getById(Long id) {
        SysPermission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("权限不存在"));
        return toDTO(permission);
    }

    @Override
    public List<PermissionDTO> getTree() {
        List<SysPermission> all = permissionRepository.findAllByOrderByCategoryAscPermissionSortAsc();
        Map<String, List<SysPermission>> grouped = all.stream()
                .collect(Collectors.groupingBy(p -> p.getCategory() != null ? p.getCategory() : "other"));

        // 分类中文名称映射
        Map<String, String> categoryNames = Map.of(
                "user", "用户管理",
                "role", "角色管理",
                "menu", "菜单管理",
                "workflow", "工作流",
                "permission", "权限管理",
                "other", "其他"
        );

        return grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> PermissionDTO.builder()
                        .name(categoryNames.getOrDefault(entry.getKey(), entry.getKey()))  // 使用中文名称
                        .category(entry.getKey())
                        .children(entry.getValue().stream().map(this::toDTO).collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<PermissionDTO> getFlatList() {
        return permissionRepository.findAllByOrderByCategoryAscPermissionSortAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private PermissionDTO toDTO(SysPermission permission) {
        return PermissionDTO.builder()
                .id(permission.getId())
                .code(permission.getCode())
                .name(permission.getName())
                .category(permission.getCategory())
                .permissionType(permission.getPermissionType())
                .description(permission.getDescription())
                .permissionSort(permission.getPermissionSort())
                .build();
    }
}
