package com.nirvanafire.ocadmin.controller;

import com.nirvanafire.ocadmin.dto.AssetDTO;
import com.nirvanafire.ocadmin.entity.SysUser;
import com.nirvanafire.ocadmin.repository.UserRepository;
import com.nirvanafire.ocadmin.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 资产管理控制器
 */
@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<AssetDTO> create(Authentication authentication, @RequestBody AssetDTO dto) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(assetService.createAsset(userId, dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetDTO> update(Authentication authentication, @PathVariable Long id, @RequestBody AssetDTO dto) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(assetService.updateAsset(userId, id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        Long userId = getCurrentUserId(authentication);
        assetService.deleteAsset(userId, id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Page<AssetDTO>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        return ResponseEntity.ok(assetService.listAssets(status, keyword, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(assetService.getAsset(id));
    }

    @PostMapping("/{id}/borrow")
    public ResponseEntity<AssetDTO> borrow(Authentication authentication, @PathVariable Long id, @RequestBody String reason) {
        Long userId = getCurrentUserId(authentication);
        String username = authentication.getName();
        return ResponseEntity.ok(assetService.applyBorrow(userId, username, id, reason));
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<AssetDTO> returnAsset(Authentication authentication, @PathVariable Long id, @RequestBody String reason) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(assetService.applyReturn(userId, id, reason));
    }

    @PostMapping("/{id}/transfer")
    public ResponseEntity<AssetDTO> transfer(Authentication authentication, @PathVariable Long id, @RequestBody java.util.Map<String, Object> body) {
        Long userId = getCurrentUserId(authentication);
        String username = authentication.getName();
        Long targetDeptId = Long.valueOf(body.get("targetDeptId").toString());
        String reason = body.get("reason").toString();
        return ResponseEntity.ok(assetService.applyTransfer(userId, username, id, targetDeptId, reason));
    }

    @PostMapping("/{id}/scrap")
    public ResponseEntity<AssetDTO> scrap(Authentication authentication, @PathVariable Long id, @RequestBody String reason) {
        Long userId = getCurrentUserId(authentication);
        String username = authentication.getName();
        return ResponseEntity.ok(assetService.applyScrap(userId, username, id, reason));
    }

    @GetMapping("/my-requests")
    public ResponseEntity<Page<AssetDTO>> myRequests(Authentication authentication, Pageable pageable) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(assetService.getMyRequests(userId, pageable));
    }

    private Long getCurrentUserId(Authentication authentication) {
        String username = authentication.getName();
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return user.getId();
    }
}
