package com.zh.service;

import com.zh.model.JobConfig;  // 引入 JobConfig 类

import java.util.Map;

/**
 * @author mumu
 */
public interface JobService {

    boolean addJob(JobConfig jobConfig);


    boolean updateJob(int jobId , JobConfig jobConfig);

    boolean removeJob(int jobId);

    boolean startJob(int jobId);

    boolean stopJob(int jobId);

    boolean triggerJob(int jobId, String executorParam);

    Map<String, Object> getJobList(int start, int length);


    boolean checkJobExists(String jobDesc, String executorHandler);  // 检查任务是否存在

}
