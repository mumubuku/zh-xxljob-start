package com.zh.config;




import com.zh.annotation.EnableXxlJob;
import com.zh.annotation.XxlJobTask;
import com.zh.service.JobService;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class XXLJobTaskRegistrar {

    @Autowired
    private JobService jobService;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private ApplicationContext applicationContext;

    private static final String JOB_REGISTRATION_LOCK = "xxl-job-registration-lock";

    @PostConstruct
    public void registerJobs() {
        // 获取主启动类，检查其是否带有 @EnableXxlJob 注解
        Class<?> mainApplicationClass = applicationContext.getBeansWithAnnotation(EnableXxlJob.class)
                .values()
                .iterator()
                .next()
                .getClass();
        EnableXxlJob enableXxlJob = AnnotationUtils.findAnnotation(mainApplicationClass, EnableXxlJob.class);

        if (enableXxlJob == null) {
            System.out.println("未找到 @EnableXxlJob 注解，跳过任务注册");
            return;
        }

        String basePackage = enableXxlJob.basePackage();
        System.out.println("扫描的包路径: " + basePackage);

        // 使用 Reflections 扫描指定包路径下的任务
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackages(basePackage)
                .addScanners(new MethodAnnotationsScanner())); // 添加 MethodAnnotationsScanner
        Set<Method> jobMethods = reflections.getMethodsAnnotatedWith(XxlJobTask.class);

        for (Method method : jobMethods) {
            XxlJobTask xxlJobTask = method.getAnnotation(XxlJobTask.class);
            String jobDesc = xxlJobTask.jobDesc();
            String executorHandler = xxlJobTask.executorHandler();

            // 获取分布式锁，防止多个节点同时添加相同任务
            RLock lock = redissonClient.getLock(JOB_REGISTRATION_LOCK);
            try {
                if (lock.tryLock(10, TimeUnit.SECONDS)) {
                    // 检查任务是否已存在
                    if (jobService.checkJobExists(jobDesc, executorHandler)) {
                        System.out.println("任务 [" + jobDesc + "] 已存在，跳过添加");
                        continue;
                    }

                    // 创建任务配置
                    Map<String, Object> jobConfig = new HashMap<>();
                    jobConfig.put("jobGroup", 1);
                    jobConfig.put("jobDesc", jobDesc);
                    jobConfig.put("author", xxlJobTask.author());
                    jobConfig.put("alarmEmail", xxlJobTask.alarmEmail());
                    jobConfig.put("executorHandler", executorHandler);
                    jobConfig.put("scheduleType", "CRON");
                    jobConfig.put("scheduleConf", xxlJobTask.cron());
                    jobConfig.put("glueType", "BEAN");
                    jobConfig.put("executorRouteStrategy", xxlJobTask.executorRouteStrategy());
                    jobConfig.put("misfireStrategy", xxlJobTask.misfireStrategy());
                    jobConfig.put("executorBlockStrategy", xxlJobTask.executorBlockStrategy());
                    jobConfig.put("executorTimeout", xxlJobTask.executorTimeout());
                    jobConfig.put("executorFailRetryCount", xxlJobTask.executorFailRetryCount());

                    // 添加任务到 XXL-JOB 管理中心
                    boolean success = jobService.addJob(jobConfig);
                    if (success) {
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
}
