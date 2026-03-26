package com.nirvanafire.ocadmin.service.impl;

import com.nirvanafire.ocadmin.service.ConfigService;
import com.nirvanafire.ocadmin.service.FileStorageService;
import org.springframework.stereotype.Component;

@Component
public class FileStorageFactory {

    private final ConfigService configService;
    private final RustfsStorageServiceImpl rustfsStorageService;
    private final AliyunOssStorageServiceImpl aliyunOssStorageService;

    public FileStorageFactory(ConfigService configService,
                              RustfsStorageServiceImpl rustfsStorageService,
                              AliyunOssStorageServiceImpl aliyunOssStorageService) {
        this.configService = configService;
        this.rustfsStorageService = rustfsStorageService;
        this.aliyunOssStorageService = aliyunOssStorageService;
    }

    public FileStorageService getStorageService() {
        String storageType = configService.getValue("storage.type", "rustfs");
        if ("oss".equalsIgnoreCase(storageType)) {
            return aliyunOssStorageService;
        }
        return rustfsStorageService;
    }
}
