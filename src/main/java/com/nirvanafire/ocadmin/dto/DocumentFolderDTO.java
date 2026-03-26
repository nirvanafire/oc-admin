package com.nirvanafire.ocadmin.dto;

import lombok.Data;

/**
 * 文档文件夹DTO
 */
@Data
public class DocumentFolderDTO {
    private Long id;
    private Long parentId;
    private String name;
    private Long ownerId;
    private String ownerName;
    private Boolean isPublic;
    private String createdAt;
    private String updatedAt;
}
