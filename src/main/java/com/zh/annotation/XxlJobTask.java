package com.zh.annotation;

import com.zh.model.ExecutorBlockStrategy;
import com.zh.model.ExecutorRouteStrategy;
import com.zh.model.MisfireStrategy;
import com.zh.model.ScheduleType;
import com.zh.model.GlueType;

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

    ScheduleType scheduleType() default ScheduleType.CRON; // 调度类型，默认为 CRON
    String cron() default "0 0 * * * ?";      // Cron 表达式，默认为每小时执行一次

    GlueType glueType() default GlueType.BEAN; // 代码类型，默认为 BEAN

    String executorHandler()    default "";             // 任务处理器名称，必须指定

    ExecutorRouteStrategy executorRouteStrategy() default ExecutorRouteStrategy.FIRST;     // 路由策略
    MisfireStrategy misfireStrategy() default MisfireStrategy.DO_NOTHING;      // 任务调度策略
    ExecutorBlockStrategy executorBlockStrategy() default ExecutorBlockStrategy.SERIAL_EXECUTION; // 阻塞策略

    int executorTimeout() default 0;                 // 任务超时时间，0 表示无限制
    int executorFailRetryCount() default 0;          // 失败重试次数
}
