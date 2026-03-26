package com.nirvanafire.ocadmin.controller;

import com.nirvanafire.ocadmin.common.Result;
import com.nirvanafire.ocadmin.entity.SysConfig;
import com.nirvanafire.ocadmin.repository.SysConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/configs")
@RequiredArgsConstructor
public class SysConfigController {

    private final SysConfigRepository configRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('config:list')")
    public Result<List<SysConfig>> getAll() {
        return Result.success(configRepository.findAll());
    }

    @GetMapping("/{key}")
    @PreAuthorize("hasAuthority('config:list')")
    public Result<SysConfig> getByKey(@PathVariable String key) {
        return configRepository.findByConfigKey(key)
                .map(Result::success)
                .orElse(Result.error(404, "配置不存在"));
    }

    @PutMapping("/{key}")
    @PreAuthorize("hasAuthority('config:update')")
    public Result<Void> update(@PathVariable String key, @RequestBody ConfigUpdateRequest request) {
        SysConfig config = configRepository.findByConfigKey(key)
                .orElse(null);
        if (config == null) {
            config = SysConfig.builder()
                    .configKey(key)
                    .configValue(request.getValue())
                    .build();
        } else {
            config.setConfigValue(request.getValue());
        }
        configRepository.save(config);
        return Result.success();
    }

    public static class ConfigUpdateRequest {
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
