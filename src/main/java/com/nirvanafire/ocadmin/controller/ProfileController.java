package com.nirvanafire.ocadmin.controller;

import com.nirvanafire.ocadmin.dto.ChangePasswordDTO;
import com.nirvanafire.ocadmin.dto.ProfileDTO;
import com.nirvanafire.ocadmin.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 个人中心控制器
 */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    /**
     * 获取当前用户资料
     */
    @GetMapping
    public ResponseEntity<ProfileDTO> getProfile(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(profileService.getProfile(username));
    }

    /**
     * 更新个人资料
     */
    @PutMapping
    public ResponseEntity<ProfileDTO> updateProfile(
            Authentication authentication,
            @RequestBody ProfileDTO dto) {
        String username = authentication.getName();
        return ResponseEntity.ok(profileService.updateProfile(username, dto));
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordDTO dto) {
        String username = authentication.getName();
        profileService.changePassword(username, dto);
        return ResponseEntity.ok().build();
    }

    /**
     * 绑定手机号
     */
    @PostMapping("/phone")
    public ResponseEntity<Void> bindPhone(
            Authentication authentication,
            @RequestParam String phone,
            @RequestParam String verifyCode) {
        String username = authentication.getName();
        profileService.bindPhone(username, phone, verifyCode);
        return ResponseEntity.ok().build();
    }
}
