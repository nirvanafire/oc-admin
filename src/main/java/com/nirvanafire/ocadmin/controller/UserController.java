package com.nirvanafire.ocadmin.controller;

import com.nirvanafire.ocadmin.common.Result;
import com.nirvanafire.ocadmin.dto.UserDTO;
import com.nirvanafire.ocadmin.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasAuthority('user:create')")
    public Result<UserDTO> create(@Valid @RequestBody UserDTO dto) {
        return Result.success(userService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user:update')")
    public Result<UserDTO> update(@PathVariable Long id, @Valid @RequestBody UserDTO dto) {
        return Result.success(userService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:view')")
    public Result<UserDTO> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('user:list')")
    public Result<Page<UserDTO>> list(@PageableDefault(size = 10) Pageable pageable) {
        return Result.success(userService.list(pageable));
    }
}
