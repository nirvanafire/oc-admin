package com.nirvanafire.ocadmin.controller;

import com.nirvanafire.ocadmin.common.Result;
import com.nirvanafire.ocadmin.dto.LoginRequest;
import com.nirvanafire.ocadmin.dto.LoginResponse;
import com.nirvanafire.ocadmin.dto.UserDTO;
import com.nirvanafire.ocadmin.security.SecurityUtils;
import com.nirvanafire.ocadmin.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            authService.logout(token.substring(7));
        }
        return Result.success();
    }

    @GetMapping("/current")
    public Result<UserDTO> getCurrentUser() {
        String username = SecurityUtils.getCurrentUsername();
        return Result.success(authService.getCurrentUser(username));
    }
}
