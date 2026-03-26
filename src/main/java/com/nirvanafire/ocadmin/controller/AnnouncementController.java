package com.nirvanafire.ocadmin.controller;

import com.nirvanafire.ocadmin.dto.AnnouncementCommentDTO;
import com.nirvanafire.ocadmin.dto.AnnouncementDTO;
import com.nirvanafire.ocadmin.entity.SysUser;
import com.nirvanafire.ocadmin.enums.AnnouncementType;
import com.nirvanafire.ocadmin.repository.UserRepository;
import com.nirvanafire.ocadmin.service.AnnouncementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 公告控制器
 */
@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;
    private final UserRepository userRepository;

    /**
     * 创建公告（草稿）
     */
    @PostMapping
    public ResponseEntity<AnnouncementDTO> create(
            Authentication authentication,
            @Valid @RequestBody AnnouncementDTO dto) {
        Long userId = getCurrentUserId(authentication);
        String username = authentication.getName();
        return ResponseEntity.ok(announcementService.create(userId, username, dto));
    }

    /**
     * 更新公告
     */
    @PutMapping("/{id}")
    public ResponseEntity<AnnouncementDTO> update(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody AnnouncementDTO dto) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(announcementService.update(userId, id, dto));
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = getCurrentUserId(authentication);
        announcementService.delete(userId, id);
        return ResponseEntity.ok().build();
    }

    /**
     * 发布公告
     */
    @PutMapping("/{id}/publish")
    public ResponseEntity<AnnouncementDTO> publish(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(announcementService.publish(userId, id));
    }

    /**
     * 下架公告
     */
    @PutMapping("/{id}/archive")
    public ResponseEntity<AnnouncementDTO> archive(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(announcementService.archive(userId, id));
    }

    /**
     * 置顶/取消置顶
     */
    @PutMapping("/{id}/top")
    public ResponseEntity<AnnouncementDTO> setTop(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam Boolean isTop,
            @RequestParam(required = false) String topExpireTime) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(announcementService.setTop(userId, id, isTop, topExpireTime));
    }

    /**
     * 获取公告详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<AnnouncementDTO> getById(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(announcementService.getById(id, userId));
    }

    /**
     * 获取公告列表（管理端）
     */
    @GetMapping("/manage")
    public ResponseEntity<Page<AnnouncementDTO>> list(
            @RequestParam(required = false) String status,
            Pageable pageable) {
        return ResponseEntity.ok(announcementService.list(status, pageable));
    }

    /**
     * 获取已发布公告列表（用户端）
     */
    @GetMapping
    public ResponseEntity<Page<AnnouncementDTO>> listPublished(
            @RequestParam(required = false) AnnouncementType type,
            Pageable pageable,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(announcementService.listPublished(type, pageable, userId));
    }

    // ==================== 阅读记录 ====================

    /**
     * 标记已读
     */
    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        announcementService.markAsRead(id, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取我的未读公告
     */
    @GetMapping("/my-unread")
    public ResponseEntity<Page<AnnouncementDTO>> getMyUnread(
            Authentication authentication,
            Pageable pageable) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(announcementService.getMyUnread(userId, pageable));
    }

    /**
     * 获取未读公告数量
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(announcementService.getUnreadCount(userId));
    }

    // ==================== 评论 ====================

    /**
     * 添加评论
     */
    @PostMapping("/{id}/comments")
    public ResponseEntity<AnnouncementCommentDTO> addComment(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @RequestBody AnnouncementCommentDTO dto) {
        Long userId = getCurrentUserId(authentication);
        String username = authentication.getName();
        dto.setAnnouncementId(id);
        return ResponseEntity.ok(announcementService.addComment(userId, username, dto));
    }

    /**
     * 获取评论列表
     */
    @GetMapping("/{id}/comments")
    public ResponseEntity<Page<AnnouncementCommentDTO>> getComments(
            @PathVariable Long id,
            Pageable pageable) {
        return ResponseEntity.ok(announcementService.getComments(id, pageable));
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        announcementService.deleteComment(userId, commentId);
        return ResponseEntity.ok().build();
    }

    // ==================== 辅助方法 ====================

    private Long getCurrentUserId(Authentication authentication) {
        String username = authentication.getName();
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return user.getId();
    }
}
