package com.zh.config;



import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "xxl.job")
@Data
public class XxlJobProperties {
    private String basePackage;
    private String adminAddresses;
    private String accessToken;
    private String username;
    private String password;
    private String appname;  // 新增 appname 配置项
    private String authMode = "session"; // 新增 authMode，默认使用 token 方式


    private String address;

    private String ip;

    private Integer port;

    private String logPath;
    private Integer logRetentionDays = 30;



}
