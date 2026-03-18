package com.nirvanafire.ocadmin.config;

import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.ProcessEngineConfigurationConfigurer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlowableConfig implements ProcessEngineConfigurationConfigurer {

    @Override
    public void configure(SpringProcessEngineConfiguration config) {
        // 禁用异步执行器，避免后台线程问题
        config.setAsyncExecutorActivate(false);
    }
}
