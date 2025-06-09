package com.zh.config;


import com.xxl.job.core.biz.AdminBiz;
import com.xxl.job.core.biz.client.AdminBizClient;
import com.xxl.job.core.biz.model.RegistryParam;
import com.xxl.job.core.enums.RegistryConfig;
import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import com.xxl.job.core.util.IpUtil;
import com.xxl.job.core.util.NetUtil;
import com.zh.service.JobService;
import com.zh.service.impl.JobServiceImpl;
import jodd.util.StringUtil;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;


@AutoConfiguration
@EnableConfigurationProperties(XxlJobProperties.class)  // 启用 XxlJobProperties 的自动装配
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


    @Bean
    @ConditionalOnMissingBean
    public XxlJobSpringExecutor xxlJobExecutor(XxlJobProperties xxlJobProperties) {
        XxlJobSpringExecutor xxlJobExecutor = new XxlJobSpringExecutor();

        // 配置执行器属性
        xxlJobExecutor.setAdminAddresses(xxlJobProperties.getAdminAddresses());
        xxlJobExecutor.setAppname(xxlJobProperties.getAppname());
        xxlJobExecutor.setIp(xxlJobProperties.getIp());

        xxlJobExecutor.setAccessToken(xxlJobProperties.getAccessToken());
       // xxlJobExecutor.setLogPath(xxlJobProperties.getLogPath());
       // xxlJobExecutor.setLogRetentionDays(xxlJobProperties.getLogRetentionDays());


        String ip = xxlJobProperties.getIp();
        if (StringUtil.isBlank(ip)) {
            ip = IpUtil.getIp();
        }

        xxlJobExecutor.setIp(ip);
        Integer port = xxlJobProperties.getPort();
        if (port == null) {
            port = NetUtil.findAvailablePort(9999);
        }

        xxlJobProperties.setPort(port);


        String logPath = xxlJobProperties.getLogPath();
        if (StringUtil.isNotBlank(logPath)) {
            xxlJobExecutor.setLogPath(logPath);
        }

        Integer logRetentionDays = xxlJobProperties.getLogRetentionDays();
        if (logRetentionDays != null) {
            xxlJobExecutor.setLogRetentionDays(logRetentionDays);
        }

        String address = xxlJobProperties.getAddress();
        if (StringUtil.isBlank(address)) {

            address = "http://{ip_port}/".replace("{ip_port}", IpUtil.getIpPort(ip, port));
        }



        RegistryParam registryParam = new RegistryParam(RegistryConfig.RegistType.EXECUTOR.name(), xxlJobProperties.getAppname(), address);
        AdminBiz adminBiz = new AdminBizClient(xxlJobProperties.getAdminAddresses().trim(), xxlJobProperties.getAccessToken().trim());

        try {
            com.xxl.job.core.biz.model.ReturnT<String> registryResult = adminBiz.registry(registryParam);
            if (registryResult != null && 200 == registryResult.getCode()) {
                registryResult = com.xxl.job.core.biz.model.ReturnT.SUCCESS;

            } else {

               // Assert.isTrue(false, ">>>>>>>>>>> xxl-job registry fail, registryParam:{}, registryResult:{}", new Object[]{registryParam, registryResult});
            }
        } catch (Exception var14) {

           // Assert.isTrue(false, ">>>>>>>>>>> xxl-job registry error, registryParam:{}", new Object[]{registryParam, var14});
        }


        return xxlJobExecutor;
    }
}
