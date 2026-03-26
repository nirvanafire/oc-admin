package com.nirvanafire.ocadmin.service;

public interface ConfigService {
    String getValue(String key);
    String getValue(String key, String defaultValue);
    void setValue(String key, String value);
}
