package com.nirvanafire.ocadmin.service.impl;

import com.nirvanafire.ocadmin.common.exception.BusinessException;
import com.nirvanafire.ocadmin.dto.DocumentFileDTO;
import com.nirvanafire.ocadmin.dto.DocumentFolderDTO;
import com.nirvanafire.ocadmin.entity.DocumentFile;
import com.nirvanafire.ocadmin.entity.DocumentFolder;
import com.nirvanafire.ocadmin.entity.DocumentVersion;
import com.nirvanafire.ocadmin.repository.*;
import com.nirvanafire.ocadmin.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文档服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentFolderRepository folderRepository;
    private final DocumentFileRepository fileRepository;
    private final DocumentVersionRepository versionRepository;
    private final DocumentShareRepository shareRepository;

    @Value("${app.upload.path:/uploads/documents}")
    private String uploadPath;

    // ==================== 文件夹管理 ====================

    @Override
    public List<DocumentFolderDTO> getFolderTree(Long userId) {
        List<DocumentFolder> allFolders = folderRepository.findAllAccessible(userId);
        return buildFolderTree(allFolders, 0L);
    }

    private List<DocumentFolderDTO> buildFolderTree(List<DocumentFolder> folders, Long parentId) {
        return folders.stream()
                .filter(f -> f.getParentId().equals(parentId))
                .map(f -> {
                    DocumentFolderDTO dto = toFolderDTO(f);
                    dto.setId(f.getId());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DocumentFolderDTO createFolder(Long userId, String username, Long parentId, String name) {
        if (parentId == null) parentId = 0L;
        
        if (parentId > 0 && !folderRepository.existsById(parentId)) {
            throw new BusinessException("父文件夹不存在");
        }

        if (folderRepository.existsByParentIdAndName(parentId, name)) {
            throw new BusinessException("文件夹已存在");
        }

        DocumentFolder folder = DocumentFolder.builder()
                .parentId(parentId)
                .name(name)
                .ownerId(userId)
                .ownerName(username)
                .isPublic(false)
                .build();

        folder = folderRepository.save(folder);
        return toFolderDTO(folder);
    }

    @Override
    @Transactional
    public DocumentFolderDTO renameFolder(Long userId, Long folderId, String name) {
        DocumentFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new BusinessException("文件夹不存在"));

        if (!folder.getOwnerId().equals(userId)) {
            throw new BusinessException("无权操作");
        }

        folder.setName(name);
        folder = folderRepository.save(folder);
        return toFolderDTO(folder);
    }

    @Override
    @Transactional
    public void deleteFolder(Long userId, Long folderId) {
        DocumentFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new BusinessException("文件夹不存在"));

        if (!folder.getOwnerId().equals(userId)) {
            throw new BusinessException("无权操作");
        }

        // 检查是否有子文件夹
        if (!folderRepository.findByParentId(folderId).isEmpty()) {
            throw new BusinessException("请先删除子文件夹");
        }

        // 检查是否有文件
        if (!fileRepository.findByFolderIdAndIsLatestTrue(folderId).isEmpty()) {
            throw new BusinessException("请先删除文件夹中的文件");
        }

        folderRepository.delete(folder);
    }

    @Override
    @Transactional
    public DocumentFolderDTO moveFolder(Long userId, Long folderId, Long newParentId) {
        DocumentFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new BusinessException("文件夹不存在"));

        if (!folder.getOwnerId().equals(userId)) {
            throw new BusinessException("无权操作");
        }

        if (newParentId != null && newParentId > 0) {
            if (!folderRepository.existsById(newParentId)) {
                throw new BusinessException("目标文件夹不存在");
            }
            // 检查不能移动到自己的子文件夹
            if (isChildFolder(folderId, newParentId)) {
                throw new BusinessException("不能移动到子文件夹");
            }
        }

        folder.setParentId(newParentId != null ? newParentId : 0L);
        folder = folderRepository.save(folder);
        return toFolderDTO(folder);
    }

    private boolean isChildFolder(Long parentId, Long childId) {
        DocumentFolder child = folderRepository.findById(childId).orElse(null);
        while (child != null && child.getParentId() != 0L) {
            if (child.getParentId().equals(parentId)) return true;
            child = folderRepository.findById(child.getParentId()).orElse(null);
        }
        return false;
    }

    // ==================== 文件管理 ====================

    @Override
    public Page<DocumentFileDTO> getFiles(Long folderId, Long userId, Pageable pageable) {
        Page<DocumentFile> page = fileRepository.findByFolderId(folderId, pageable);
        List<DocumentFileDTO> list = page.getContent().stream()
                .map(this::toFileDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(list, pageable, page.getTotalElements());
    }

    @Override
    public DocumentFileDTO getFile(Long fileId, Long userId) {
        DocumentFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException("文件不存在"));
        return toFileDTO(file);
    }

    @Override
    @Transactional
    public DocumentFileDTO uploadFile(Long userId, String username, Long folderId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String fileExt = getFileExtension(originalFilename);
        String fileName = getFileNameWithoutExt(originalFilename);
        
        // 检查文件是否已存在
        if (fileRepository.findByFolderIdAndFileNameAndIsLatestTrue(folderId, originalFilename).isPresent()) {
            throw new BusinessException("文件已存在，请先删除或重命名");
        }

        // 生成存储路径
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String savePath = String.format("%s/%s/%s", uploadPath, datePath, UUID.randomUUID() + "." + fileExt);
        
        // 保存文件（实际项目中需要调用文件存储服务）
        // 这里简化处理
        
        DocumentFile documentFile = DocumentFile.builder()
                .folderId(folderId)
                .fileName(originalFilename)
                .fileSize(file.getSize())
                .fileType(getFileType(fileExt))
                .filePath(savePath)
                .fileUrl("/api/doc/files/download/" + savePath)
                .fileExt(fileExt)
                .ownerId(userId)
                .ownerName(username)
                .version(1)
                .isLatest(true)
                .downloadCount(0)
                .build();

        documentFile = fileRepository.save(documentFile);
        return toFileDTO(documentFile);
    }

    @Override
    @Transactional
    public void deleteFile(Long userId, Long fileId) {
        DocumentFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException("文件不存在"));

        if (!file.getOwnerId().equals(userId)) {
            throw new BusinessException("无权操作");
        }

        // 删除所有版本
        versionRepository.deleteByFileId(fileId);
        
        // 删除共享记录
        shareRepository.deleteByFileId(fileId);
        
        fileRepository.delete(file);
    }

    @Override
    @Transactional
    public DocumentFileDTO renameFile(Long userId, Long fileId, String name) {
        DocumentFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException("文件不存在"));

        if (!file.getOwnerId().equals(userId)) {
            throw new BusinessException("无权操作");
        }

        file.setFileName(name);
        file = fileRepository.save(file);
        return toFileDTO(file);
    }

    @Override
    @Transactional
    public DocumentFileDTO moveFile(Long userId, Long fileId, Long newFolderId) {
        DocumentFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException("文件不存在"));

        if (!file.getOwnerId().equals(userId)) {
            throw new BusinessException("无权操作");
        }

        if (newFolderId != null && !folderRepository.existsById(newFolderId)) {
            throw new BusinessException("目标文件夹不存在");
        }

        file.setFolderId(newFolderId != null ? newFolderId : 0L);
        file = fileRepository.save(file);
        return toFileDTO(file);
    }

    @Override
    public Page<DocumentFileDTO> search(Long userId, String keyword, Pageable pageable) {
        Page<DocumentFile> page = fileRepository.search(keyword, pageable);
        List<DocumentFileDTO> list = page.getContent().stream()
                .map(this::toFileDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(list, pageable, page.getTotalElements());
    }

    // ==================== 版本管理 ====================

    @Override
    public List<Object> getVersionHistory(Long fileId) {
        List<DocumentVersion> versions = versionRepository.findByFileIdOrderByVersionDesc(fileId);
        return versions.stream().map(v -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", v.getId());
            map.put("version", v.getVersion());
            map.put("fileSize", v.getFileSize());
            map.put("comment", v.getComment());
            map.put("creatorName", v.getCreatorName());
            map.put("createdAt", v.getCreatedAt());
            return (Object) map;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DocumentFileDTO createVersion(Long userId, String username, Long fileId, MultipartFile file, String comment) {
        DocumentFile originalFile = fileRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException("文件不存在"));

        if (!originalFile.getOwnerId().equals(userId)) {
            throw new BusinessException("无权操作");
        }

        // 获取最新版本号
        DocumentVersion latestVersion = versionRepository.findTopByFileIdOrderByVersionDesc(fileId)
                .orElse(null);
        int newVersion = (latestVersion != null ? latestVersion.getVersion() : 0) + 1;

        // 保存旧版本文件信息
        DocumentVersion oldVersion = DocumentVersion.builder()
                .fileId(fileId)
                .version(originalFile.getVersion())
                .fileSize(originalFile.getFileSize())
                .filePath(originalFile.getFilePath())
                .fileUrl(originalFile.getFileUrl())
                .comment(comment)
                .creatorId(userId)
                .creatorName(username)
                .build();
        versionRepository.save(oldVersion);

        // 更新当前文件
        originalFile.setFileSize(file.getSize());
        originalFile.setVersion(newVersion);
        originalFile.setIsLatest(true);
        
        originalFile = fileRepository.save(originalFile);
        return toFileDTO(originalFile);
    }

    @Override
    @Transactional
    public DocumentFileDTO rollbackVersion(Long userId, Long fileId, Integer version) {
        DocumentFile file = fileRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException("文件不存在"));

        if (!file.getOwnerId().equals(userId)) {
            throw new BusinessException("无权操作");
        }

        DocumentVersion targetVersion = versionRepository.findByFileIdAndVersion(fileId, version)
                .orElseThrow(() -> new BusinessException("版本不存在"));

        // 将当前版本保存到历史
        DocumentVersion currentVersion = DocumentVersion.builder()
                .fileId(fileId)
                .version(file.getVersion())
                .fileSize(file.getFileSize())
                .filePath(file.getFilePath())
                .fileUrl(file.getFileUrl())
                .comment("回滚前版本")
                .creatorId(userId)
                .creatorName("系统")
                .build();
        versionRepository.save(currentVersion);

        // 恢复到目标版本
        file.setFileSize(targetVersion.getFileSize());
        file.setFilePath(targetVersion.getFilePath());
        file.setFileUrl(targetVersion.getFileUrl());
        file.setVersion(version);
        
        file = fileRepository.save(file);
        return toFileDTO(file);
    }

    // ==================== 辅助方法 ====================

    private DocumentFolderDTO toFolderDTO(DocumentFolder folder) {
        DocumentFolderDTO dto = new DocumentFolderDTO();
        dto.setId(folder.getId());
        dto.setParentId(folder.getParentId());
        dto.setName(folder.getName());
        dto.setOwnerId(folder.getOwnerId());
        dto.setOwnerName(folder.getOwnerName());
        dto.setIsPublic(folder.getIsPublic());
        return dto;
    }

    private DocumentFileDTO toFileDTO(DocumentFile file) {
        DocumentFileDTO dto = new DocumentFileDTO();
        dto.setId(file.getId());
        dto.setFolderId(file.getFolderId());
        dto.setFileName(file.getFileName());
        dto.setFileSize(file.getFileSize());
        dto.setFileSizeDesc(formatFileSize(file.getFileSize()));
        dto.setFileType(file.getFileType());
        dto.setFileUrl(file.getFileUrl());
        dto.setFileExt(file.getFileExt());
        dto.setOwnerId(file.getOwnerId());
        dto.setOwnerName(file.getOwnerName());
        dto.setVersion(file.getVersion());
        dto.setIsLatest(file.getIsLatest());
        dto.setDownloadCount(file.getDownloadCount());
        dto.setTags(file.getTags());
        return dto;
    }

    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1).toLowerCase() : "";
    }

    private String getFileNameWithoutExt(String filename) {
        if (filename == null) return "";
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(0, lastDot) : filename;
    }

    private String getFileType(String ext) {
        if (ext == null) return "unknown";
        return switch (ext.toLowerCase()) {
            case "pdf" -> "pdf";
            case "doc", "docx" -> "word";
            case "xls", "xlsx" -> "excel";
            case "ppt", "pptx" -> "ppt";
            case "jpg", "jpeg", "png", "gif", "bmp" -> "image";
            case "mp4", "avi", "mov" -> "video";
            case "mp3", "wav", "flac" -> "audio";
            case "txt", "md", "json", "xml" -> "text";
            case "zip", "rar", "7z" -> "archive";
            default -> "other";
        };
    }

    private String formatFileSize(Long size) {
        if (size == null) return "0 B";
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / 1024.0 / 1024.0);
        return String.format("%.1f GB", size / 1024.0 / 1024.0 / 1024.0);
    }
}
