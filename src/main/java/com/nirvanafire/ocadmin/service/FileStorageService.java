package com.nirvanafire.ocadmin.service;

public interface FileStorageService {
    String upload(byte[] data, String filename);
    String uploadImageWithWatermark(byte[] data, String filename);
    void delete(String url);
    String getAccessUrl(String key);
    byte[] getFile(String key);
}
