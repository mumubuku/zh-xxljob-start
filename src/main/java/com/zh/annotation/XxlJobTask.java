package com.zh.annotation;



import java.lang.annotation.*;

/**
 * @author mumu
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface XxlJobTask {
    String jobDesc();                         // 任务描述
    String author() default "admin";          // 任务负责人
    String alarmEmail() default "";           // 报警邮件

    String scheduleType() default  "CRON";
    String cron() default "0 0 * * * ?";      // Cron 表达式，默认为每小时执行一次


    String glueType() default "BEAN";

    String executorHandler();                 // 任务处理器名称，必须指定

    String executorRouteStrategy() default "FIRST";     // 路由策略
    String misfireStrategy() default "DO_NOTHING";      // 任务调度策略
    String executorBlockStrategy() default "SERIAL_EXECUTION"; // 阻塞策略

    int executorTimeout() default 0;                 // 任务超时时间，0 表示无限制
    int executorFailRetryCount() default 0;          // 失败重试次数
}
