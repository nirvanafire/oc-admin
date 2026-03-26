package com.nirvanafire.ocadmin.service.impl;

import com.nirvanafire.ocadmin.entity.SysConfig;
import com.nirvanafire.ocadmin.repository.SysConfigRepository;
import com.nirvanafire.ocadmin.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {

    private final SysConfigRepository configRepository;

    @Override
    public String getValue(String key) {
        return configRepository.findByConfigKey(key)
                .map(SysConfig::getConfigValue)
                .orElse(null);
    }

    @Override
    public String getValue(String key, String defaultValue) {
        return configRepository.findByConfigKey(key)
                .map(SysConfig::getConfigValue)
                .orElse(defaultValue);
    }

    @Override
    @Transactional
    public void setValue(String key, String value) {
        SysConfig config = configRepository.findByConfigKey(key)
                .orElse(SysConfig.builder().configKey(key).build());
        config.setConfigValue(value);
        configRepository.save(config);
    }
}
