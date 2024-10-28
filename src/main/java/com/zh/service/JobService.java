package com.zh.service;

import java.util.Map;


/**
 * @author mumu
 */
public interface JobService {

    boolean addJob(Map<String, Object> jobConfig);

    boolean updateJob(Map<String, Object> jobConfig);

    boolean removeJob(int jobId);

    boolean startJob(int jobId);

    boolean stopJob(int jobId);

    boolean triggerJob(int jobId, String executorParam);

    Map<String, Object> getJobList(int start, int length);


    boolean checkJobExists(String jobDesc, String executorHandler);  // 检查任务是否存在

}
