package com.zh.config;

import com.zh.annotation.EnableXxlJob;
import com.zh.annotation.XxlJobTask;
import com.zh.model.*;
import com.zh.service.JobService;
import jakarta.annotation.PostConstruct;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;


import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class XXLJobTaskRegistrar {

    private static final String JOB_REGISTRATION_LOCK = "xxl-job-registration-lock";

    @Autowired
    private JobService jobService;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private ApplicationContext applicationContext;


    @Autowired
    private XxlJobProperties xxlJobProperties;

    @PostConstruct
    public void registerJobs() {
        // 获取主启动类，检查其是否带有 @EnableXxlJob 注解
        Object mainAppBean = applicationContext.getBeansWithAnnotation(EnableXxlJob.class).values().stream().findFirst().orElse(null);
        if (mainAppBean == null) {
            throw new IllegalStateException("未找到使用 @EnableXxlJob 注解的类");
        }

        EnableXxlJob enableXxlJob = AnnotationUtils.findAnnotation(mainAppBean.getClass(), EnableXxlJob.class);
        String basePackage = enableXxlJob.basePackage();
        System.out.println("扫描的包路径: " + basePackage);

        // 使用 Reflections 扫描指定包路径下的任务
        Reflections reflections = new Reflections(basePackage, new MethodAnnotationsScanner());
        Set<Method> jobMethods = reflections.getMethodsAnnotatedWith(XxlJobTask.class);

        for (Method method : jobMethods) {
            XxlJobTask xxlJobTask = method.getAnnotation(XxlJobTask.class);
            String jobDesc = xxlJobTask.jobDesc();
            String executorHandler = xxlJobTask.executorHandler();

            if (jobDesc == null || jobDesc.trim().isEmpty()) {
                throw new IllegalArgumentException("@XxlJobTask on " + method + " 必须指定 jobDesc");
            }

            if (xxlJobTask.cron() == null || xxlJobTask.cron().trim().isEmpty()) {
                throw new IllegalArgumentException("@XxlJobTask on " + method + " 必须指定 cron 表达式");
            }
            if (executorHandler == null || executorHandler.isEmpty()) {
                executorHandler = method.getName();
            }


            // 获取分布式锁，防止多个节点同时添加相同任务
            RLock lock = redissonClient.getLock(JOB_REGISTRATION_LOCK);
            try {
                if (lock.tryLock(10, TimeUnit.SECONDS)) {
                    // 检查任务是否已存在
                    if (jobService.checkJobExists(jobDesc, executorHandler)) {
                        System.out.println("任务 [" + jobDesc + "] 已存在，跳过添加");
                        continue;
                    }

                    // 创建 JobConfig 对象并设置属性
                    JobConfig jobConfig = buildJobConfig(xxlJobTask, jobDesc, executorHandler);
                    Integer id = jobService.addJob(jobConfig);
                    Boolean isSuccess = jobService.startJob(id);
                    if (isSuccess) {
                        System.out.println("任务 [" + jobDesc + "] 成功添加到 XXL-JOB 管理中心");
                    } else {
                        System.out.println("任务 [" + jobDesc + "] 添加失败");
                    }
                } else {
                    System.out.println("获取锁失败，跳过任务注册: " + jobDesc);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        }
    }

    private JobConfig buildJobConfig(XxlJobTask xxlJobTask, String jobDesc, String executorHandler) {
        JobConfig jobConfig = new JobConfig();
        jobConfig.setJobGroup(jobService.getJobGroupIdByAppname(xxlJobProperties.getAppname())); // 设置执行器组ID
        jobConfig.setJobDesc(jobDesc); // 设置作业描述
        jobConfig.setExecutorHandler(executorHandler); // 设置执行器Handler
        jobConfig.setAuthor(xxlJobTask.author()); // 设置作业作者
        jobConfig.setScheduleType(xxlJobTask.scheduleType()); // 设置调度类型
        jobConfig.setScheduleConf(xxlJobTask.cron()); // 设置调度配置（CRON表达式）
        jobConfig.setGlueType(xxlJobTask.glueType()); // 设置代码类型
        jobConfig.setExecutorRouteStrategy(xxlJobTask.executorRouteStrategy()); // 设置执行路由策略
        jobConfig.setMisfireStrategy(xxlJobTask.misfireStrategy()); // 设置错过调度时的策略
        jobConfig.setExecutorBlockStrategy(xxlJobTask.executorBlockStrategy()); // 设置执行阻塞策略
        jobConfig.setExecutorTimeout(xxlJobTask.executorTimeout()); // 设置执行超时时间
        jobConfig.setExecutorFailRetryCount(xxlJobTask.executorFailRetryCount()); // 设置失败重试次数
        return jobConfig;
    }
}
