package com.zh.aspect;

import com.xxl.job.core.context.XxlJobHelper;
import com.zh.annotation.RetryableJob;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.springframework.stereotype.Component;

/**
 * @BelongsProject: zh-xxljob-starter
 * @BelongsPackage: com.zh.aspect
 * @Author: mumu
 * @CreateTime: 2024-11-18  17:17
 * @Description: TODO
 * @Version: 1.0
 */
@Component
class RetryAspect {

    @Around("@annotation(retryableJob)")
    public Object around(ProceedingJoinPoint joinPoint, RetryableJob retryableJob) throws Throwable {
        int maxAttempts = retryableJob.maxAttempts(); // 最大重试次数
        long delay = retryableJob.delay(); // 每次重试之间的延迟时间

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                XxlJobHelper.log("第 " + attempt + " 次尝试执行方法 " + joinPoint.getSignature().getName());
                return joinPoint.proceed(); // 执行被代理的方法
            } catch (Exception e) {
                XxlJobHelper.log("第 " + attempt + " 次尝试失败: " + e.getMessage());

                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(delay); // 等待一段时间后重试
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        XxlJobHelper.log("线程在重试延迟期间被中断。");
                        throw ie;
                    }
                } else {
                    XxlJobHelper.log("方法 " + joinPoint.getSignature().getName() + " 在 " + maxAttempts + " 次尝试后最终失败。");
                    throw e; // 最终失败，抛出异常
                }
            }
        }
        return null; // 不会执行到这里，因为重试成功或者最终失败会提前返回或抛出异常
    }
}
