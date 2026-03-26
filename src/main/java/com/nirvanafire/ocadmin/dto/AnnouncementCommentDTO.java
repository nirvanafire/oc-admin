package com.nirvanafire.ocadmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告评论DTO
 */
@Data
public class AnnouncementCommentDTO {
    private Long id;
    
    @NotNull(message = "公告ID不能为空")
    private Long announcementId;
    
    private Long userId;
    private String userName;
    private Long parentId;
    
    @NotBlank(message = "评论内容不能为空")
    private String content;
    
    private LocalDateTime createdAt;
}
