package com.zh.config;


import com.zh.service.JobService;
import com.zh.service.impl.JobServiceImpl;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(XxlJobProperties.class)  // 启用 XxlJobProperties 的自动装配
@ConditionalOnClass(JobService.class)  // 当项目中有 XxlJobService 时才生效
public class XxlJobAutoConfiguration {

    /**
     * 注册任务到 XXL-JOB 的管理中心
     */
    @Bean
    @ConditionalOnMissingBean
    public XXLJobTaskRegistrar xxlJobTaskRegistrar(JobService jobService, RedissonClient redissonClient) {
        return new XXLJobTaskRegistrar();
    }

    /**
     * 如果没有自定义的 XxlJobService，则提供默认实现
     */
    @Bean
    @ConditionalOnMissingBean
    public JobService jobService() {
        return new JobServiceImpl();
    }
}
