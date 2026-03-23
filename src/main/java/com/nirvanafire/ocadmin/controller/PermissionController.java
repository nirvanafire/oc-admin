package com.nirvanafire.ocadmin.controller;

import com.nirvanafire.ocadmin.common.Result;
import com.nirvanafire.ocadmin.dto.PermissionDTO;
import com.nirvanafire.ocadmin.dto.PermissionRequest;
import com.nirvanafire.ocadmin.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('permission:list')")
    public Result<List<PermissionDTO>> getTree() {
        return Result.success(permissionService.getTree());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('permission:list')")
    public Result<List<PermissionDTO>> getFlatList() {
        return Result.success(permissionService.getFlatList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:list')")
    public Result<PermissionDTO> getById(@PathVariable Long id) {
        return Result.success(permissionService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('permission:create')")
    public Result<PermissionDTO> create(@Valid @RequestBody PermissionRequest request) {
        return Result.success(permissionService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:update')")
    public Result<PermissionDTO> update(@PathVariable Long id, @Valid @RequestBody PermissionRequest request) {
        return Result.success(permissionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return Result.success(null);
    }
}
