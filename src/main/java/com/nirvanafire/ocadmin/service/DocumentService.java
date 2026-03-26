package com.nirvanafire.ocadmin.service;

import com.nirvanafire.ocadmin.dto.DocumentFileDTO;
import com.nirvanafire.ocadmin.dto.DocumentFolderDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文档服务接口
 */
public interface DocumentService {
    
    // ==================== 文件夹管理 ====================
    
    /**
     * 获取文件夹树
     */
    List<DocumentFolderDTO> getFolderTree(Long userId);
    
    /**
     * 创建文件夹
     */
    DocumentFolderDTO createFolder(Long userId, String username, Long parentId, String name);
    
    /**
     * 重命名文件夹
     */
    DocumentFolderDTO renameFolder(Long userId, Long folderId, String name);
    
    /**
     * 删除文件夹
     */
    void deleteFolder(Long userId, Long folderId);
    
    /**
     * 移动文件夹
     */
    DocumentFolderDTO moveFolder(Long userId, Long folderId, Long newParentId);
    
    // ==================== 文件管理 ====================
    
    /**
     * 获取文件列表
     */
    Page<DocumentFileDTO> getFiles(Long folderId, Long userId, Pageable pageable);
    
    /**
     * 获取文件详情
     */
    DocumentFileDTO getFile(Long fileId, Long userId);
    
    /**
     * 上传文件
     */
    DocumentFileDTO uploadFile(Long userId, String username, Long folderId, MultipartFile file);
    
    /**
     * 删除文件
     */
    void deleteFile(Long userId, Long fileId);
    
    /**
     * 重命名文件
     */
    DocumentFileDTO renameFile(Long userId, Long fileId, String name);
    
    /**
     * 移动文件
     */
    DocumentFileDTO moveFile(Long userId, Long fileId, Long newFolderId);
    
    /**
     * 搜索文件
     */
    Page<DocumentFileDTO> search(Long userId, String keyword, Pageable pageable);
    
    // ==================== 版本管理 ====================
    
    /**
     * 获取文件版本历史
     */
    List<Object> getVersionHistory(Long fileId);
    
    /**
     * 创建新版本
     */
    DocumentFileDTO createVersion(Long userId, String username, Long fileId, MultipartFile file, String comment);
    
    /**
     * 回滚到指定版本
     */
    DocumentFileDTO rollbackVersion(Long userId, Long fileId, Integer version);
}
