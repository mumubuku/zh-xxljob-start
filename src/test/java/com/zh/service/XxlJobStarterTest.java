package com.zh.service;

import com.zh.config.XXLJobTaskRegistrar;
import com.zh.model.*;
import com.zh.config.XxlJobProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "xxl.job.admin-addresses=http://36.139.142.158:8081/xxl-job-admin",
        "xxl.job.appname=gp-order",
        "xxl.job.accessToken=your-access-token",
        "xxl.job.username=admin",
        "xxl.job.password=BJyp@1909",
})
@ActiveProfiles("test")  // 使用测试环境配置
public class XxlJobStarterTest {

    @Autowired
    private JobService xxlJobService;

    @Autowired
    private XXLJobTaskRegistrar taskRegistrar;

    @Autowired
    private XxlJobProperties xxlJobProperties;

    @Test
    public void testAddJob() {
        // 使用 JobConfig 对象来创建任务配置
        JobConfig jobConfig = new JobConfig();
        jobConfig.setJobGroup(16);  // 使用有效的执行器组ID
        jobConfig.setJobDesc("Test Job");
        jobConfig.setExecutorHandler("testHandler");
        jobConfig.setScheduleType(ScheduleType.CRON);  // 使用枚举
        jobConfig.setScheduleConf("0 0/1 * * * ?");  // 每分钟执行一次
        jobConfig.setExecutorRouteStrategy(ExecutorRouteStrategy.FIRST);  // 使用枚举
        jobConfig.setExecutorBlockStrategy(ExecutorBlockStrategy.SERIAL_EXECUTION);  // 使用枚举
        jobConfig.setGlueType(GlueType.BEAN);  // 使用枚举
        jobConfig.setAuthor("tester");
        jobConfig.setExecutorTimeout(60);
        jobConfig.setExecutorFailRetryCount(1);
        jobConfig.setMisfireStrategy(MisfireStrategy.DO_NOTHING);
        jobConfig.setFailStrategy(FailStrategy.FAIL_FAST);

        System.out.println(jobConfig);
        // 调用 addJob 方法，假设成功返回 true
        Integer result = xxlJobService.addJob(jobConfig);

        // 断言返回值为 true，表示任务添加成功
        assertTrue(result > 0, "Job should be added successfully");
    }


    @Test
    public void testUpdateJob() {
        // 使用 JobConfig 对象来创建任务配置
        JobConfig jobConfig = new JobConfig();
        jobConfig.setJobGroup(16);  // 使用有效的执行器组ID
        jobConfig.setJobDesc("Updated Test Job");
        jobConfig.setExecutorHandler("updatedHandler");
        jobConfig.setScheduleType(ScheduleType.CRON);  // 使用枚举
        jobConfig.setScheduleConf("0 0/2 * * * ?");  // 每两分钟执行一次
        jobConfig.setExecutorRouteStrategy(ExecutorRouteStrategy.FIRST);  // 使用枚举
        jobConfig.setExecutorBlockStrategy(ExecutorBlockStrategy.SERIAL_EXECUTION);  // 使用枚举
        jobConfig.setGlueType(GlueType.BEAN);  // 使用枚举
        jobConfig.setAuthor("tester_updated");
        jobConfig.setExecutorTimeout(120);
        jobConfig.setExecutorFailRetryCount(2);
        jobConfig.setMisfireStrategy(MisfireStrategy.DO_NOTHING);
        jobConfig.setFailStrategy(FailStrategy.FAIL_FAST);

        System.out.println(jobConfig);
        // 调用 updateJob 方法，假设成功返回 true
        boolean result = xxlJobService.updateJob(6976,jobConfig);

        // 断言返回值为 true，表示任务更新成功
        assertTrue(result, "Job should be updated successfully");
    }


    @Test
    public void testRemoveJob() {
        boolean result =  xxlJobService.removeJob(6975);

        assertTrue(result, "Job should be updated successfully");
    }


    @Test
    public void testStartJob() {
        boolean result =  xxlJobService.startJob(6974);

        assertTrue(result, "Job should be updated successfully");
    }


    @Test
    public void testStopJob() {
        boolean result =  xxlJobService.stopJob(6974);

        assertTrue(result, "Job should be updated successfully");
    }


    @Test
    public void tiggerJob() {
        boolean result =  xxlJobService.triggerJob(6974,"test111");

        assertTrue(result, "Job should be updated successfully");
    }


    @Test
    public void testGetJobList() {
        // 获取任务列表，假设从服务端获取
        Map<String, Object> jobList = xxlJobService.getJobList(TriggerStatus.TRIGGERED);

        // 断言返回值不为 null
        assertNotNull(jobList, "Job list should not be null");

        // 断言 jobList 中包含 "data" 键
        assertTrue(jobList.containsKey("data"), "Job list should contain 'data' key");
    }

    @Test
    public void testJobTrigger() {
        int jobId = 1;  // 需要替换为有效的任务 ID
        boolean result = xxlJobService.triggerJob(jobId, "11");

        // 断言任务触发结果为 true
        assertTrue(result, "Job should be triggered successfully");
    }


    @Test
    public void testGetappId() {
        int jobId = 1;  // 需要替换为有效的任务 ID
        int result = xxlJobService.getJobGroupIdByAppname("gp-order");

        // 断言任务触发结果为 true
        assertTrue(result == 16, "Job should be triggered successfully");
    }

}
