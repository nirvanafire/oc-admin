package com.nirvanafire.ocadmin.service;

import com.nirvanafire.ocadmin.dto.AnnouncementCommentDTO;
import com.nirvanafire.ocadmin.dto.AnnouncementDTO;
import com.nirvanafire.ocadmin.enums.AnnouncementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 公告服务接口
 */
public interface AnnouncementService {
    
    // ==================== 公告管理 ====================
    
    /**
     * 创建公告（草稿）
     */
    AnnouncementDTO create(Long userId, String username, AnnouncementDTO dto);
    
    /**
     * 更新公告
     */
    AnnouncementDTO update(Long userId, Long id, AnnouncementDTO dto);
    
    /**
     * 删除公告
     */
    void delete(Long userId, Long id);
    
    /**
     * 发布公告
     */
    AnnouncementDTO publish(Long userId, Long id);
    
    /**
     * 下架公告
     */
    AnnouncementDTO archive(Long userId, Long id);
    
    /**
     * 置顶/取消置顶
     */
    AnnouncementDTO setTop(Long userId, Long id, Boolean isTop, String topExpireTime);
    
    /**
     * 获取公告详情
     */
    AnnouncementDTO getById(Long id, Long userId);
    
    /**
     * 获取公告列表（管理端）
     */
    Page<AnnouncementDTO> list(String status, Pageable pageable);
    
    /**
     * 获取已发布公告列表（用户端）
     */
    Page<AnnouncementDTO> listPublished(AnnouncementType type, Pageable pageable, Long userId);
    
    // ==================== 阅读记录 ====================
    
    /**
     * 标记已读
     */
    void markAsRead(Long announcementId, Long userId);
    
    /**
     * 获取我的未读公告
     */
    Page<AnnouncementDTO> getMyUnread(Long userId, Pageable pageable);
    
    /**
     * 获取未读公告数量
     */
    Long getUnreadCount(Long userId);
    
    // ==================== 评论 ====================
    
    /**
     * 添加评论
     */
    AnnouncementCommentDTO addComment(Long userId, String username, AnnouncementCommentDTO dto);
    
    /**
     * 删除评论
     */
    void deleteComment(Long userId, Long commentId);
    
    /**
     * 获取评论列表
     */
    Page<AnnouncementCommentDTO> getComments(Long announcementId, Pageable pageable);
}
