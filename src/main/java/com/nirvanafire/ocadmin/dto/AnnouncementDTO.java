package com.nirvanafire.ocadmin.dto;

import com.nirvanafire.ocadmin.enums.AnnouncementStatus;
import com.nirvanafire.ocadmin.enums.AnnouncementType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告DTO
 */
@Data
public class AnnouncementDTO {
    private Long id;
    
    @NotBlank(message = "标题不能为空")
    private String title;
    
    private String content;
    private String summary;
    
    @NotNull(message = "公告类型不能为空")
    private AnnouncementType announcementType;
    
    private String coverImage;
    private Boolean isTop;
    private LocalDateTime topExpireTime;
    private Boolean allowComment;
    private AnnouncementStatus status;
    private String statusDesc;
    private Long publisherId;
    private String publisherName;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
    private Boolean isRead; // 当前用户是否已读
}
