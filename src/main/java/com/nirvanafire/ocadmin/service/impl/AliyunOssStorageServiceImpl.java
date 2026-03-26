package com.nirvanafire.ocadmin.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.*;
import com.nirvanafire.ocadmin.service.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class AliyunOssStorageServiceImpl extends AbstractFileStorageService {

    public AliyunOssStorageServiceImpl(ConfigService configService, ImageWatermarkServiceImpl imageWatermarkService) {
        super(configService, imageWatermarkService);
    }

    @Override
    public String upload(byte[] data, String filename) {
        String bucket = configService.getValue("storage.oss.bucket");
        String endpoint = configService.getValue("storage.oss.endpoint");
        String accessKeyId = configService.getValue("storage.oss.access-key");
        String accessKeySecret = configService.getValue("storage.oss.secret-key");
        String key = generateKey(filename);

        OSS ossClient = createOssClient(endpoint, accessKeyId, accessKeySecret);
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(getContentType(filename));
            PutObjectRequest putRequest = new PutObjectRequest(bucket, key, new ByteArrayInputStream(data), metadata);
            ossClient.putObject(putRequest);
            log.info("文件上传成功: {}", key);
            return key;
        } finally {
            ossClient.shutdown();
        }
    }

    @Override
    public void delete(String url) {
        String bucket = configService.getValue("storage.oss.bucket");
        String endpoint = configService.getValue("storage.oss.endpoint");
        String accessKeyId = configService.getValue("storage.oss.access-key");
        String accessKeySecret = configService.getValue("storage.oss.secret-key");
        String key = extractKey(url);

        OSS ossClient = createOssClient(endpoint, accessKeyId, accessKeySecret);
        try {
            ossClient.deleteObject(bucket, key);
            log.info("文件删除成功: {}", key);
        } finally {
            ossClient.shutdown();
        }
    }

    @Override
    public String getAccessUrl(String key) {
        String bucket = configService.getValue("storage.oss.bucket");
        String endpoint = configService.getValue("storage.oss.endpoint");
        String accessKeyId = configService.getValue("storage.oss.access-key");
        String accessKeySecret = configService.getValue("storage.oss.secret-key");

        OSS ossClient = createOssClient(endpoint, accessKeyId, accessKeySecret);
        try {
            Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000);
            URL url = ossClient.generatePresignedUrl(bucket, key, expiration);
            return url.toString();
        } finally {
            ossClient.shutdown();
        }
    }

    public byte[] download(String key) {
        String bucket = configService.getValue("storage.oss.bucket");
        String endpoint = configService.getValue("storage.oss.endpoint");
        String accessKeyId = configService.getValue("storage.oss.access-key");
        String accessKeySecret = configService.getValue("storage.oss.secret-key");

        OSS ossClient = createOssClient(endpoint, accessKeyId, accessKeySecret);
        try {
            OSSObject ossObject = ossClient.getObject(bucket, key);
            return ossObject.getObjectContent().readAllBytes();
        } catch (IOException e) {
            log.error("文件下载失败: {}", key, e);
            throw new RuntimeException("文件下载失败", e);
        } finally {
            ossClient.shutdown();
        }
    }

    @Override
    public byte[] getFile(String key) {
        return download(key);
    }

    private OSS createOssClient(String endpoint, String accessKeyId, String accessKeySecret) {
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

    private String generateKey(String filename) {
        String extension = "";
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = filename.substring(dotIndex);
        }
        return "images/" + UUID.randomUUID().toString() + extension;
    }

    private String extractKey(String url) {
        if (url.contains("/images/")) {
            String key = url.substring(url.indexOf("/images/"));
            // Remove leading slash if present
            if (key.startsWith("/")) {
                key = key.substring(1);
            }
            return key;
        }
        return url;
    }

    private String getContentType(String filename) {
        String extension = "";
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = filename.substring(dotIndex).toLowerCase();
        }
        return switch (extension) {
            case ".png" -> "image/png";
            case ".gif" -> "image/gif";
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".webp" -> "image/webp";
            case ".bmp" -> "image/bmp";
            default -> "application/octet-stream";
        };
    }
}
