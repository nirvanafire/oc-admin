package com.nirvanafire.ocadmin.controller;

import com.nirvanafire.ocadmin.common.Result;
import com.nirvanafire.ocadmin.dto.DeptDTO;
import com.nirvanafire.ocadmin.dto.UserDTO;
import com.nirvanafire.ocadmin.entity.SysUser;
import com.nirvanafire.ocadmin.repository.UserRepository;
import com.nirvanafire.ocadmin.service.DeptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/depts")
@RequiredArgsConstructor
public class DeptController {

    private final DeptService deptService;
    private final UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasAuthority('dept:create')")
    public Result<DeptDTO> create(@Valid @RequestBody DeptDTO dto) {
        return Result.success(deptService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('dept:update')")
    public Result<DeptDTO> update(@PathVariable Long id, @Valid @RequestBody DeptDTO dto) {
        return Result.success(deptService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('dept:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        deptService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('dept:view')")
    public Result<DeptDTO> getById(@PathVariable Long id) {
        return Result.success(deptService.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('dept:list')")
    public Result<Page<DeptDTO>> list(
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(required = false) String deptName) {
        return Result.success(deptService.list(pageable, deptName));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('dept:list')")
    public Result<List<DeptDTO>> getAll() {
        return Result.success(deptService.getAll());
    }

    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('dept:list')")
    public Result<List<DeptDTO>> getTree() {
        return Result.success(deptService.getTree());
    }

    @GetMapping("/{id}/children")
    @PreAuthorize("hasAuthority('dept:list')")
    public Result<List<DeptDTO>> getChildren(@PathVariable Long id) {
        return Result.success(deptService.getChildren(id));
    }

    @GetMapping("/{id}/users")
    @PreAuthorize("hasAuthority('dept:list')")
    public Result<List<UserDTO>> getDeptUsers(@PathVariable Long id) {
        List<Long> userIds = deptService.getUserIdsByDeptId(id);
        List<UserDTO> users = userIds.stream()
                .map(userRepository::findById)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .map(this::toUserDTO)
                .collect(Collectors.toList());
        return Result.success(users);
    }

    @GetMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('dept:list')")
    public Result<List<Long>> getDeptRoles(@PathVariable Long id) {
        return Result.success(deptService.getRoleIdsByDeptId(id));
    }

    @PostMapping("/{id}/users/{userId}")
    @PreAuthorize("hasAuthority('dept:update')")
    public Result<Void> addUserToDept(@PathVariable Long id, @PathVariable Long userId) {
        deptService.addUserToDept(id, userId);
        return Result.success();
    }

    @DeleteMapping("/{id}/users/{userId}")
    @PreAuthorize("hasAuthority('dept:update')")
    public Result<Void> removeUserFromDept(@PathVariable Long id, @PathVariable Long userId) {
        deptService.removeUserFromDept(id, userId);
        return Result.success();
    }

    @PostMapping("/{id}/roles/{roleId}")
    @PreAuthorize("hasAuthority('dept:update')")
    public Result<Void> addRoleToDept(@PathVariable Long id, @PathVariable Long roleId) {
        deptService.addRoleToDept(id, roleId);
        return Result.success();
    }

    @DeleteMapping("/{id}/roles/{roleId}")
    @PreAuthorize("hasAuthority('dept:update')")
    public Result<Void> removeRoleFromDept(@PathVariable Long id, @PathVariable Long roleId) {
        deptService.removeRoleFromDept(id, roleId);
        return Result.success();
    }

    private UserDTO toUserDTO(SysUser user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAvatar(user.getAvatar());
        dto.setEnabled(user.getEnabled());
        return dto;
    }
}
