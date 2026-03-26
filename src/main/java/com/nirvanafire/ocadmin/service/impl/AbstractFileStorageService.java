package com.nirvanafire.ocadmin.service.impl;

import com.nirvanafire.ocadmin.service.ConfigService;
import com.nirvanafire.ocadmin.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

public abstract class AbstractFileStorageService implements FileStorageService {

    protected final ConfigService configService;
    protected final ImageWatermarkServiceImpl imageWatermarkService;

    public AbstractFileStorageService(ConfigService configService, ImageWatermarkServiceImpl imageWatermarkService) {
        this.configService = configService;
        this.imageWatermarkService = imageWatermarkService;
    }

    @Override
    public String uploadImageWithWatermark(byte[] data, String filename) {
        String watermarkText = configService.getValue("watermark.text", "");
        boolean watermarkEnabled = Boolean.parseBoolean(configService.getValue("watermark.enabled", "true"));
        if (watermarkEnabled && StringUtils.hasText(watermarkText)) {
            data = imageWatermarkService.addWatermark(data, watermarkText);
        }
        return upload(data, filename);
    }
}
