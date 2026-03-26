package com.nirvanafire.ocadmin.dto;

import lombok.Data;

/**
 * 文档文件DTO
 */
@Data
public class DocumentFileDTO {
    private Long id;
    private Long folderId;
    private String folderName;
    private String fileName;
    private Long fileSize;
    private String fileSizeDesc;
    private String fileType;
    private String fileUrl;
    private String fileExt;
    private Long ownerId;
    private String ownerName;
    private Integer version;
    private Boolean isLatest;
    private Integer downloadCount;
    private String tags;
    private String createdAt;
    private String updatedAt;
}
