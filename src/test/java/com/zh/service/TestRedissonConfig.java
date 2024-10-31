package com.zh.service;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestRedissonConfig {

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
       
        return Redisson.create(config);
    }
}

