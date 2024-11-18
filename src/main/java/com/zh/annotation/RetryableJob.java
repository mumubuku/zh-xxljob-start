package com.zh.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 定义一个注解，用于标记需要重试的任务方法
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RetryableJob {
    // 最大重试次数，默认值为3
    int maxAttempts() default 3;
    // 每次重试之间的延迟时间，默认值为1000毫秒
    long delay() default 1000;
}

