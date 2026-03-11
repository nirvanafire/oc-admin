package com.nirvanafire.ocadmin.controller;

import com.nirvanafire.ocadmin.common.Result;
import com.nirvanafire.ocadmin.dto.RoleDTO;
import com.nirvanafire.ocadmin.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    @PreAuthorize("hasAuthority('role:create')")
    public Result<RoleDTO> create(@Valid @RequestBody RoleDTO dto) {
        return Result.success(roleService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('role:update')")
    public Result<RoleDTO> update(@PathVariable Long id, @Valid @RequestBody RoleDTO dto) {
        return Result.success(roleService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('role:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('role:view')")
    public Result<RoleDTO> getById(@PathVariable Long id) {
        return Result.success(roleService.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('role:list')")
    public Result<Page<RoleDTO>> list(
            @PageableDefault(size = 10) Pageable pageable,
            @RequestParam(required = false) String code) {
        return Result.success(roleService.list(pageable, code));
    }

    @GetMapping("/all")
    public Result<List<RoleDTO>> all() {
        return Result.success(roleService.all());
    }
}
