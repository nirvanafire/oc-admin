package com.nirvanafire.ocadmin.service.impl;

import com.nirvanafire.ocadmin.common.exception.BusinessException;
import com.nirvanafire.ocadmin.dto.AnnouncementCommentDTO;
import com.nirvanafire.ocadmin.dto.AnnouncementDTO;
import com.nirvanafire.ocadmin.entity.Announcement;
import com.nirvanafire.ocadmin.entity.AnnouncementComment;
import com.nirvanafire.ocadmin.entity.AnnouncementRead;
import com.nirvanafire.ocadmin.enums.AnnouncementStatus;
import com.nirvanafire.ocadmin.enums.AnnouncementType;
import com.nirvanafire.ocadmin.repository.*;
import com.nirvanafire.ocadmin.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 公告服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final AnnouncementReadRepository announcementReadRepository;
    private final AnnouncementCommentRepository announcementCommentRepository;

    // ==================== 公告管理 ====================

    @Override
    @Transactional
    public AnnouncementDTO create(Long userId, String username, AnnouncementDTO dto) {
        Announcement announcement = Announcement.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .summary(dto.getSummary())
                .announcementType(dto.getAnnouncementType())
                .coverImage(dto.getCoverImage())
                .allowComment(dto.getAllowComment() != null ? dto.getAllowComment() : false)
                .isTop(false)
                .status(AnnouncementStatus.DRAFT)
                .publisherId(userId)
                .publisherName(username)
                .viewCount(0)
                .build();

        announcement = announcementRepository.save(announcement);
        return toDTO(announcement, userId);
    }

    @Override
    @Transactional
    public AnnouncementDTO update(Long userId, Long id, AnnouncementDTO dto) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new BusinessException("公告不存在"));

        if (!announcement.getPublisherId().equals(userId)) {
            throw new BusinessException("无权操作");
        }

        if (announcement.getStatus() == AnnouncementStatus.PUBLISHED) {
            throw new BusinessException("已发布的公告不能修改");
        }

        if (dto.getTitle() != null) announcement.setTitle(dto.getTitle());
        if (dto.getContent() != null) announcement.setContent(dto.getContent());
        if (dto.getSummary() != null) announcement.setSummary(dto.getSummary());
        if (dto.getAnnouncementType() != null) announcement.setAnnouncementType(dto.getAnnouncementType());
        if (dto.getCoverImage() != null) announcement.setCoverImage(dto.getCoverImage());
        if (dto.getAllowComment() != null) announcement.setAllowComment(dto.getAllowComment());

        announcement = announcementRepository.save(announcement);
        return toDTO(announcement, userId);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new BusinessException("公告不存在"));

        if (!announcement.getPublisherId().equals(userId)) {
            throw new BusinessException("无权操作");
        }

        announcementRepository.delete(announcement);
    }

    @Override
    @Transactional
    public AnnouncementDTO publish(Long userId, Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new BusinessException("公告不存在"));

        if (!announcement.getPublisherId().equals(userId)) {
            throw new BusinessException("无权操作");
        }

        if (announcement.getStatus() == AnnouncementStatus.PUBLISHED) {
            throw new BusinessException("公告已发布");
        }

        announcement.setStatus(AnnouncementStatus.PUBLISHED);
        announcement.setPublishedAt(LocalDateTime.now());

        announcement = announcementRepository.save(announcement);
        return toDTO(announcement, userId);
    }

    @Override
    @Transactional
    public AnnouncementDTO archive(Long userId, Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new BusinessException("公告不存在"));

        if (!announcement.getPublisherId().equals(userId)) {
            throw new BusinessException("无权操作");
        }

        announcement.setStatus(AnnouncementStatus.ARCHIVED);
        announcement = announcementRepository.save(announcement);
        return toDTO(announcement, userId);
    }

    @Override
    @Transactional
    public AnnouncementDTO setTop(Long userId, Long id, Boolean isTop, String topExpireTime) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new BusinessException("公告不存在"));

        if (!announcement.getPublisherId().equals(userId)) {
            throw new BusinessException("无权操作");
        }

        announcement.setIsTop(isTop);
        if (isTop && topExpireTime != null) {
            announcement.setTopExpireTime(LocalDateTime.parse(topExpireTime));
        } else {
            announcement.setTopExpireTime(null);
        }

        announcement = announcementRepository.save(announcement);
        return toDTO(announcement, userId);
    }

    @Override
    public AnnouncementDTO getById(Long id, Long userId) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new BusinessException("公告不存在"));
        
        // 增加浏览次数
        if (announcement.getStatus() == AnnouncementStatus.PUBLISHED) {
            announcement.setViewCount(announcement.getViewCount() + 1);
            announcementRepository.save(announcement);
        }
        
        return toDTO(announcement, userId);
    }

    @Override
    public Page<AnnouncementDTO> list(String status, Pageable pageable) {
        Page<Announcement> page;
        if (status != null && !status.isEmpty()) {
            page = announcementRepository.findByStatus(AnnouncementStatus.valueOf(status), pageable);
        } else {
            page = announcementRepository.findAll(pageable);
        }

        List<AnnouncementDTO> list = page.getContent().stream()
                .map(a -> toDTO(a, null))
                .collect(Collectors.toList());

        return new PageImpl<>(list, pageable, page.getTotalElements());
    }

    @Override
    public Page<AnnouncementDTO> listPublished(AnnouncementType type, Pageable pageable, Long userId) {
        Page<Announcement> page;
        if (type != null) {
            page = announcementRepository.findPublishedByType(type, pageable);
        } else {
            page = announcementRepository.findPublishedOrderByTopAndTime(LocalDateTime.now(), pageable);
        }

        List<AnnouncementDTO> list = page.getContent().stream()
                .map(a -> toDTO(a, userId))
                .collect(Collectors.toList());

        return new PageImpl<>(list, pageable, page.getTotalElements());
    }

    // ==================== 阅读记录 ====================

    @Override
    @Transactional
    public void markAsRead(Long announcementId, Long userId) {
        if (announcementReadRepository.findByAnnouncementIdAndUserId(announcementId, userId).isPresent()) {
            return; // 已读
        }

        AnnouncementRead read = AnnouncementRead.builder()
                .announcementId(announcementId)
                .userId(userId)
                .build();

        announcementReadRepository.save(read);
    }

    @Override
    public Page<AnnouncementDTO> getMyUnread(Long userId, Pageable pageable) {
        // 获取已读列表
        List<Long> readIds = announcementReadRepository.findAnnouncementIdsByUserId(userId);
        
        // 获取已发布公告
        Page<Announcement> page = announcementRepository.findPublishedOrderByTopAndTime(
                LocalDateTime.now(), pageable);
        
        // 过滤未读
        List<AnnouncementDTO> list = page.getContent().stream()
                .filter(a -> !readIds.contains(a.getId()))
                .map(a -> toDTO(a, userId))
                .collect(Collectors.toList());

        return new PageImpl<>(list, pageable, page.getTotalElements());
    }

    @Override
    public Long getUnreadCount(Long userId) {
        // 获取所有已发布的公告数
        Page<Announcement> allPublished = announcementRepository.findPublishedOrderByTopAndTime(
                LocalDateTime.now(), Pageable.ofSize(Integer.MAX_VALUE));
        
        // 获取已读列表
        List<Long> readIds = announcementReadRepository.findAnnouncementIdsByUserId(userId);
        
        return allPublished.getTotalElements() - readIds.size();
    }

    // ==================== 评论 ====================

    @Override
    @Transactional
    public AnnouncementCommentDTO addComment(Long userId, String username, AnnouncementCommentDTO dto) {
        Announcement announcement = announcementRepository.findById(dto.getAnnouncementId())
                .orElseThrow(() -> new BusinessException("公告不存在"));

        if (!announcement.getAllowComment()) {
            throw new BusinessException("该公告不允许评论");
        }

        AnnouncementComment comment = AnnouncementComment.builder()
                .announcementId(dto.getAnnouncementId())
                .userId(userId)
                .userName(username)
                .parentId(dto.getParentId())
                .content(dto.getContent())
                .build();

        comment = announcementCommentRepository.save(comment);
        return toCommentDTO(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        AnnouncementComment comment = announcementCommentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException("评论不存在"));

        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("无权删除");
        }

        announcementCommentRepository.delete(comment);
    }

    @Override
    public Page<AnnouncementCommentDTO> getComments(Long announcementId, Pageable pageable) {
        Page<AnnouncementComment> page = announcementCommentRepository
                .findByAnnouncementIdOrderByCreatedAtDesc(announcementId, pageable);

        List<AnnouncementCommentDTO> list = page.getContent().stream()
                .map(this::toCommentDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(list, pageable, page.getTotalElements());
    }

    // ==================== 转换方法 ====================

    private AnnouncementDTO toDTO(Announcement a, Long userId) {
        AnnouncementDTO dto = new AnnouncementDTO();
        dto.setId(a.getId());
        dto.setTitle(a.getTitle());
        dto.setContent(a.getContent());
        dto.setSummary(a.getSummary());
        dto.setAnnouncementType(a.getAnnouncementType());
        dto.setCoverImage(a.getCoverImage());
        dto.setIsTop(a.getIsTop());
        dto.setTopExpireTime(a.getTopExpireTime());
        dto.setAllowComment(a.getAllowComment());
        dto.setStatus(a.getStatus());
        dto.setStatusDesc(a.getStatus().getDescription());
        dto.setPublisherId(a.getPublisherId());
        dto.setPublisherName(a.getPublisherName());
        dto.setViewCount(a.getViewCount());
        dto.setCreatedAt(a.getCreatedAt());
        dto.setPublishedAt(a.getPublishedAt());
        
        if (userId != null) {
            dto.setIsRead(announcementReadRepository.findByAnnouncementIdAndUserId(a.getId(), userId).isPresent());
        }
        
        return dto;
    }

    private AnnouncementCommentDTO toCommentDTO(AnnouncementComment c) {
        AnnouncementCommentDTO dto = new AnnouncementCommentDTO();
        dto.setId(c.getId());
        dto.setAnnouncementId(c.getAnnouncementId());
        dto.setUserId(c.getUserId());
        dto.setUserName(c.getUserName());
        dto.setParentId(c.getParentId());
        dto.setContent(c.getContent());
        dto.setCreatedAt(c.getCreatedAt());
        return dto;
    }
}
