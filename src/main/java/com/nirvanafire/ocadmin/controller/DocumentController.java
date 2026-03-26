package com.nirvanafire.ocadmin.controller;

import com.nirvanafire.ocadmin.dto.DocumentFileDTO;
import com.nirvanafire.ocadmin.dto.DocumentFolderDTO;
import com.nirvanafire.ocadmin.entity.SysUser;
import com.nirvanafire.ocadmin.repository.UserRepository;
import com.nirvanafire.ocadmin.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 文档管理控制器
 */
@RestController
@RequestMapping("/api/doc")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;
    private final UserRepository userRepository;

    // ==================== 文件夹管理 ====================

    /**
     * 获取文件夹树
     */
    @GetMapping("/folders/tree")
    public ResponseEntity<List<DocumentFolderDTO>> getFolderTree(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(documentService.getFolderTree(userId));
    }

    /**
     * 创建文件夹
     */
    @PostMapping("/folders")
    public ResponseEntity<DocumentFolderDTO> createFolder(
            Authentication authentication,
            @RequestBody Map<String, Object> body) {
        Long userId = getCurrentUserId(authentication);
        String username = authentication.getName();
        Long parentId = body.get("parentId") != null ? Long.valueOf(body.get("parentId").toString()) : 0L;
        String name = body.get("name").toString();
        return ResponseEntity.ok(documentService.createFolder(userId, username, parentId, name));
    }

    /**
     * 重命名文件夹
     */
    @PutMapping("/folders/{id}")
    public ResponseEntity<DocumentFolderDTO> renameFolder(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(documentService.renameFolder(userId, id, body.get("name")));
    }

    /**
     * 删除文件夹
     */
    @DeleteMapping("/folders/{id}")
    public ResponseEntity<Void> deleteFolder(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = getCurrentUserId(authentication);
        documentService.deleteFolder(userId, id);
        return ResponseEntity.ok().build();
    }

    /**
     * 移动文件夹
     */
    @PutMapping("/folders/{id}/move")
    public ResponseEntity<DocumentFolderDTO> moveFolder(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        Long userId = getCurrentUserId(authentication);
        Long newParentId = body.get("parentId");
        return ResponseEntity.ok(documentService.moveFolder(userId, id, newParentId));
    }

    // ==================== 文件管理 ====================

    /**
     * 获取文件列表
     */
    @GetMapping("/files")
    public ResponseEntity<Page<DocumentFileDTO>> getFiles(
            @RequestParam Long folderId,
            Authentication authentication,
            Pageable pageable) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(documentService.getFiles(folderId, userId, pageable));
    }

    /**
     * 获取文件详情
     */
    @GetMapping("/files/{id}")
    public ResponseEntity<DocumentFileDTO> getFile(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(documentService.getFile(id, userId));
    }

    /**
     * 上传文件
     */
    @PostMapping("/files")
    public ResponseEntity<DocumentFileDTO> uploadFile(
            Authentication authentication,
            @RequestParam Long folderId,
            @RequestParam("file") MultipartFile file) {
        Long userId = getCurrentUserId(authentication);
        String username = authentication.getName();
        return ResponseEntity.ok(documentService.uploadFile(userId, username, folderId, file));
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/files/{id}")
    public ResponseEntity<Void> deleteFile(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = getCurrentUserId(authentication);
        documentService.deleteFile(userId, id);
        return ResponseEntity.ok().build();
    }

    /**
     * 重命名文件
     */
    @PutMapping("/files/{id}/rename")
    public ResponseEntity<DocumentFileDTO> renameFile(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(documentService.renameFile(userId, id, body.get("name")));
    }

    /**
     * 移动文件
     */
    @PutMapping("/files/{id}/move")
    public ResponseEntity<DocumentFileDTO> moveFile(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        Long userId = getCurrentUserId(authentication);
        Long newFolderId = body.get("folderId");
        return ResponseEntity.ok(documentService.moveFile(userId, id, newFolderId));
    }

    /**
     * 搜索文件
     */
    @GetMapping("/search")
    public ResponseEntity<Page<DocumentFileDTO>> search(
            @RequestParam String keyword,
            Authentication authentication,
            Pageable pageable) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(documentService.search(userId, keyword, pageable));
    }

    // ==================== 版本管理 ====================

    /**
     * 获取版本历史
     */
    @GetMapping("/files/{id}/versions")
    public ResponseEntity<List<Object>> getVersionHistory(@PathVariable Long id) {
        return ResponseEntity.ok(documentService.getVersionHistory(id));
    }

    /**
     * 创建新版本
     */
    @PostMapping("/files/{id}/versions")
    public ResponseEntity<DocumentFileDTO> createVersion(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String comment) {
        Long userId = getCurrentUserId(authentication);
        String username = authentication.getName();
        return ResponseEntity.ok(documentService.createVersion(userId, username, id, file, comment));
    }

    /**
     * 回滚版本
     */
    @PutMapping("/files/{id}/versions/{vid}/rollback")
    public ResponseEntity<DocumentFileDTO> rollbackVersion(
            Authentication authentication,
            @PathVariable Long id,
            @PathVariable Integer vid) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(documentService.rollbackVersion(userId, id, vid));
    }

    // ==================== 辅助方法 ====================

    private Long getCurrentUserId(Authentication authentication) {
        String username = authentication.getName();
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return user.getId();
    }
}
