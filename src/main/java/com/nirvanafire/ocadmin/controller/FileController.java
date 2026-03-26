package com.nirvanafire.ocadmin.controller;

import com.nirvanafire.ocadmin.common.Result;
import com.nirvanafire.ocadmin.service.FileStorageService;
import com.nirvanafire.ocadmin.service.impl.FileStorageFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileStorageFactory fileStorageFactory;

    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('workflow:request')")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        try {
            FileStorageService storageService = fileStorageFactory.getStorageService();
            byte[] data = file.getBytes();
            String key = storageService.uploadImageWithWatermark(data, file.getOriginalFilename());
            String accessUrl = storageService.getAccessUrl(key);
            log.info("文件上传成功: {}, 访问URL: {}", key, accessUrl);
            return Result.success(accessUrl);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('workflow:request')")
    public Result<Void> delete(@RequestParam("url") String url) {
        try {
            FileStorageService storageService = fileStorageFactory.getStorageService();
            storageService.delete(url);
            return Result.success();
        } catch (Exception e) {
            log.error("文件删除失败", e);
            return Result.error("文件删除失败: " + e.getMessage());
        }
    }
}
