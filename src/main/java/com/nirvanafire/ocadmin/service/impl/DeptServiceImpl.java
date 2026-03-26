package com.nirvanafire.ocadmin.service.impl;

import com.nirvanafire.ocadmin.common.exception.BusinessException;
import com.nirvanafire.ocadmin.dto.DeptDTO;
import com.nirvanafire.ocadmin.entity.SysDept;
import com.nirvanafire.ocadmin.entity.SysDeptRole;
import com.nirvanafire.ocadmin.entity.SysUser;
import com.nirvanafire.ocadmin.entity.SysUserDept;
import com.nirvanafire.ocadmin.repository.DeptRepository;
import com.nirvanafire.ocadmin.repository.DeptRoleRepository;
import com.nirvanafire.ocadmin.repository.RoleRepository;
import com.nirvanafire.ocadmin.repository.UserDeptRepository;
import com.nirvanafire.ocadmin.repository.UserRepository;
import com.nirvanafire.ocadmin.service.DeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeptServiceImpl implements DeptService {

    private final DeptRepository deptRepository;
    private final UserDeptRepository userDeptRepository;
    private final DeptRoleRepository deptRoleRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public DeptDTO create(DeptDTO dto) {
        if (StringUtils.hasText(dto.getDeptCode()) && deptRepository.existsByDeptCode(dto.getDeptCode())) {
            throw new BusinessException("部门编码已存在");
        }

        if (dto.getParentId() == null) {
            dto.setParentId(0L);
        }

        if (dto.getParentId() != 0L) {
            if (!deptRepository.existsById(dto.getParentId())) {
                throw new BusinessException("父部门不存在");
            }
        }

        SysDept dept = SysDept.builder()
                .parentId(dto.getParentId() != null ? dto.getParentId() : 0L)
                .deptName(dto.getDeptName())
                .deptCode(dto.getDeptCode())
                .managerId(dto.getManagerId())
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .status(dto.getStatus() != null ? dto.getStatus() : 1)
                .description(dto.getDescription())
                .build();

        dept = deptRepository.save(dept);
        return toDTO(dept);
    }

    @Override
    @Transactional
    public DeptDTO update(Long id, DeptDTO dto) {
        SysDept dept = deptRepository.findById(id)
                .orElseThrow(() -> new BusinessException("部门不存在"));

        if (StringUtils.hasText(dto.getDeptCode()) && !dto.getDeptCode().equals(dept.getDeptCode())) {
            if (deptRepository.existsByDeptCode(dto.getDeptCode())) {
                throw new BusinessException("部门编码已存在");
            }
            dept.setDeptCode(dto.getDeptCode());
        }

        if (StringUtils.hasText(dto.getDeptName())) {
            dept.setDeptName(dto.getDeptName());
        }

        if (dto.getParentId() != null) {
            if (dto.getParentId().equals(id)) {
                throw new BusinessException("不能将自己设为父部门");
            }
            if (dto.getParentId() != 0L && !deptRepository.existsById(dto.getParentId())) {
                throw new BusinessException("父部门不存在");
            }
            dept.setParentId(dto.getParentId());
        }

        if (dto.getManagerId() != null) {
            dept.setManagerId(dto.getManagerId());
        }

        if (dto.getSortOrder() != null) {
            dept.setSortOrder(dto.getSortOrder());
        }

        if (dto.getStatus() != null) {
            dept.setStatus(dto.getStatus());
        }

        if (dto.getDescription() != null) {
            dept.setDescription(dto.getDescription());
        }

        dept = deptRepository.save(dept);
        return toDTO(dept);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!deptRepository.existsById(id)) {
            throw new BusinessException("部门不存在");
        }

        if (deptRepository.existsByParentId(id)) {
            throw new BusinessException("该部门存在子部门，无法删除");
        }

        if (userDeptRepository.findUserIdsByDeptId(id) != null &&
                !userDeptRepository.findUserIdsByDeptId(id).isEmpty()) {
            throw new BusinessException("该部门存在用户，无法删除");
        }

        deptRepository.deleteById(id);
    }

    @Override
    public DeptDTO getById(Long id) {
        SysDept dept = deptRepository.findById(id)
                .orElseThrow(() -> new BusinessException("部门不存在"));
        return toDTO(dept);
    }

    @Override
    public Page<DeptDTO> list(Pageable pageable, String deptName) {
        Page<SysDept> page;
        if (StringUtils.hasText(deptName)) {
            page = deptRepository.findAll(pageable);
            List<SysDept> filtered = page.getContent().stream()
                    .filter(d -> d.getDeptName().contains(deptName))
                    .collect(Collectors.toList());
            List<DeptDTO> list = filtered.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
            return new PageImpl<>(list, pageable, page.getTotalElements());
        } else {
            page = deptRepository.findAll(pageable);
        }
        List<DeptDTO> list = page.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(list, pageable, page.getTotalElements());
    }

    @Override
    public List<DeptDTO> getTree() {
        List<SysDept> rootDepts = deptRepository.findRootDepts();
        return rootDepts.stream()
                .map(this::buildTree)
                .collect(Collectors.toList());
    }

    @Override
    public List<DeptDTO> getAll() {
        List<SysDept> allDepts = deptRepository.findAll();
        return allDepts.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<DeptDTO> getChildren(Long parentId) {
        List<SysDept> children = deptRepository.findByParentIdOrderBySortOrderAsc(parentId);
        return children.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> getUserIdsByDeptId(Long deptId) {
        return userDeptRepository.findUserIdsByDeptId(deptId);
    }

    @Override
    public List<Long> getRoleIdsByDeptId(Long deptId) {
        return deptRoleRepository.findRoleIdsByDeptId(deptId);
    }

    @Override
    @Transactional
    public void addUserToDept(Long deptId, Long userId) {
        if (!deptRepository.existsById(deptId)) {
            throw new BusinessException("部门不存在");
        }
        if (!userRepository.existsById(userId)) {
            throw new BusinessException("用户不存在");
        }
        if (userDeptRepository.existsByUserIdAndDeptId(userId, deptId)) {
            throw new BusinessException("用户已在该部门中");
        }

        SysUserDept userDept = SysUserDept.builder()
                .userId(userId)
                .deptId(deptId)
                .build();
        userDeptRepository.save(userDept);
    }

    @Override
    @Transactional
    public void removeUserFromDept(Long deptId, Long userId) {
        if (!userDeptRepository.existsByUserIdAndDeptId(userId, deptId)) {
            throw new BusinessException("用户不在该部门中");
        }
        userDeptRepository.deleteByUserIdAndDeptId(userId, deptId);
    }

    @Override
    @Transactional
    public void addRoleToDept(Long deptId, Long roleId) {
        if (!deptRepository.existsById(deptId)) {
            throw new BusinessException("部门不存在");
        }
        if (!roleRepository.existsById(roleId)) {
            throw new BusinessException("角色不存在");
        }
        if (deptRoleRepository.existsByDeptIdAndRoleId(deptId, roleId)) {
            throw new BusinessException("该部门已关联此角色");
        }

        SysDeptRole deptRole = SysDeptRole.builder()
                .deptId(deptId)
                .roleId(roleId)
                .build();
        deptRoleRepository.save(deptRole);
    }

    @Override
    @Transactional
    public void removeRoleFromDept(Long deptId, Long roleId) {
        if (!deptRoleRepository.existsByDeptIdAndRoleId(deptId, roleId)) {
            throw new BusinessException("该部门未关联此角色");
        }
        deptRoleRepository.deleteByDeptIdAndRoleId(deptId, roleId);
    }

    private DeptDTO toDTO(SysDept dept) {
        DeptDTO dto = DeptDTO.builder()
                .id(dept.getId())
                .parentId(dept.getParentId())
                .deptName(dept.getDeptName())
                .deptCode(dept.getDeptCode())
                .managerId(dept.getManagerId())
                .sortOrder(dept.getSortOrder())
                .status(dept.getStatus())
                .description(dept.getDescription())
                .createTime(dept.getCreateTime())
                .updateTime(dept.getUpdateTime())
                .build();

        if (dept.getParentId() != null && dept.getParentId() != 0L) {
            deptRepository.findById(dept.getParentId())
                    .ifPresent(parent -> dto.setParentName(parent.getDeptName()));
        }

        if (dept.getManagerId() != null) {
            userRepository.findById(dept.getManagerId())
                    .ifPresent(manager -> dto.setManagerName(
                            manager.getNickname() != null ? manager.getNickname() : manager.getUsername()));
        }

        return dto;
    }

    private DeptDTO buildTree(SysDept dept) {
        DeptDTO dto = toDTO(dept);
        List<SysDept> children = deptRepository.findByParentIdOrderBySortOrderAsc(dept.getId());
        if (!children.isEmpty()) {
            dto.setChildren(children.stream()
                    .map(this::buildTree)
                    .collect(Collectors.toList()));
        } else {
            dto.setChildren(new ArrayList<>());
        }
        return dto;
    }
}
