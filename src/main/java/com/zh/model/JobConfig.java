package com.zh.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class JobConfig implements Serializable {
    private Integer id; // 任务ID，自动生成，或者手动设置
    private Integer jobGroup; // 执行器组ID
    private String jobDesc; // 作业描述
    private String executorHandler; // 执行器的Handler
    private ScheduleType scheduleType; // 调度类型，使用枚举
    private String scheduleConf; // 调度配置，CRON表达式
    private ExecutorRouteStrategy executorRouteStrategy; // 执行路由策略，使用枚举
    private ExecutorBlockStrategy executorBlockStrategy; // 执行阻塞策略，使用枚举
    private GlueType glueType; // 代码类型，使用枚举
    private String author; // 作业作者
    private Integer executorTimeout; // 执行超时时间（单位秒）
    private Integer executorFailRetryCount; // 失败重试次数
    private List<Integer> childJobIds; // 子任务ID列表（如果有）
    private MisfireStrategy misfireStrategy; // 错过调度时的策略，使用枚举
    private FailStrategy failStrategy; // 失败后的处理策略，使用枚举
    private String  executorParam;  //执行参数


    @Override
    public String toString() {
        return "JobConfig{" +
                "id=" + id +
                ", jobGroup=" + jobGroup +
                ", jobDesc='" + jobDesc + '\'' +
                ", executorHandler='" + executorHandler + '\'' +
                ", scheduleType=" + scheduleType +
                ", scheduleConf='" + scheduleConf + '\'' +
                ", executorRouteStrategy=" + executorRouteStrategy +
                ", executorBlockStrategy=" + executorBlockStrategy +
                ", glueType=" + glueType +
                ", author='" + author + '\'' +
                ", executorTimeout=" + executorTimeout +
                ", executorFailRetryCount=" + executorFailRetryCount +
                ", childJobIds=" + childJobIds +
                ", misfireStrategy=" + misfireStrategy +
                ", failStrategy=" + failStrategy +
                '}';
    }
}
