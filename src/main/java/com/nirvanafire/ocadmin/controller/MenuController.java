package com.nirvanafire.ocadmin.controller;

import com.nirvanafire.ocadmin.common.Result;
import com.nirvanafire.ocadmin.dto.MenuDTO;
import com.nirvanafire.ocadmin.security.SecurityUtils;
import com.nirvanafire.ocadmin.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @PostMapping
    @PreAuthorize("hasAuthority('menu:create')")
    public Result<MenuDTO> create(@Valid @RequestBody MenuDTO dto) {
        return Result.success(menuService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('menu:update')")
    public Result<MenuDTO> update(@PathVariable Long id, @Valid @RequestBody MenuDTO dto) {
        return Result.success(menuService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('menu:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('menu:view')")
    public Result<MenuDTO> getById(@PathVariable Long id) {
        return Result.success(menuService.getById(id));
    }

    @GetMapping("/tree")
    public Result<List<MenuDTO>> tree() {
        return Result.success(menuService.getTree());
    }

    @GetMapping("/user")
    public Result<List<MenuDTO>> getUserMenus() {
        String username = SecurityUtils.getCurrentUsername();
        return Result.success(menuService.getUserMenus(username));
    }
}
