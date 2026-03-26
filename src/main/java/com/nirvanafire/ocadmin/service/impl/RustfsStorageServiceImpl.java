package com.nirvanafire.ocadmin.service.impl;

import com.nirvanafire.ocadmin.service.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@Service
@Slf4j
public class RustfsStorageServiceImpl extends AbstractFileStorageService {

    public RustfsStorageServiceImpl(ConfigService configService, ImageWatermarkServiceImpl imageWatermarkService) {
        super(configService, imageWatermarkService);
    }

    @Override
    public String upload(byte[] data, String filename) {
        String bucket = configService.getValue("storage.rustfs.bucket");
        String key = generateKey(filename);

        S3Client s3Client = createS3Client();
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(getContentType(filename))
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(data));
            log.info("文件上传成功: {}", key);
            return key;
        } finally {
            s3Client.close();
        }
    }

    @Override
    public void delete(String url) {
        String bucket = configService.getValue("storage.rustfs.bucket");
        String key = extractKey(url);

        S3Client s3Client = createS3Client();
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            s3Client.deleteObject(deleteRequest);
            log.info("文件删除成功: {}", key);
        } finally {
            s3Client.close();
        }
    }

    @Override
    public String getAccessUrl(String key) {
        String endpoint = configService.getValue("storage.rustfs.endpoint");
        String bucket = configService.getValue("storage.rustfs.bucket");
        return endpoint + "/" + bucket + "/" + key;
    }

    public byte[] download(String key) {
        String bucket = configService.getValue("storage.rustfs.bucket");
        S3Client s3Client = createS3Client();
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(getRequest);
            return response.readAllBytes();
        } catch (IOException e) {
            log.error("文件下载失败: {}", key, e);
            throw new RuntimeException("文件下载失败", e);
        } finally {
            s3Client.close();
        }
    }

    @Override
    public byte[] getFile(String key) {
        return download(key);
    }

    private S3Client createS3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(configService.getValue("storage.rustfs.endpoint")))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                configService.getValue("storage.rustfs.access-key"),
                                configService.getValue("storage.rustfs.secret-key")
                        )
                ))
                .forcePathStyle(true)
                .build();
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
