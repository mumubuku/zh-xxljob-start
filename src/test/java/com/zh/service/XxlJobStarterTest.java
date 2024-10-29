package com.zh.service;



import com.zh.config.XXLJobTaskRegistrar;
import com.zh.service.JobService;
import com.zh.config.XxlJobProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest(properties = {
        "xxl.job.admin-addresses=http://localhost:8080/xxl-job-admin",
        "xxl.job.appname=xxl-job-starter-test",
        "xxl.job.accessToken=your-access-token",
        "xxl.job.authMode=token"
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
        Map<String, Object> jobConfig = new HashMap<>();
        jobConfig.put("jobGroup", 1);  // 使用有效的执行器组ID
        jobConfig.put("jobDesc", "Test Job");
        jobConfig.put("executorHandler", "testHandler");
        jobConfig.put("scheduleType", "CRON");
        jobConfig.put("scheduleConf", "0 0/1 * * * ?");  // 每分钟执行一次
        jobConfig.put("executorRouteStrategy", "FIRST");
        jobConfig.put("executorBlockStrategy", "SERIAL_EXECUTION");
        jobConfig.put("glueType", "BEAN");
        jobConfig.put("author", "tester");
        jobConfig.put("executorTimeout", 60);
        jobConfig.put("executorFailRetryCount", 1);

        boolean result = xxlJobService.addJob(jobConfig);
        assertTrue(result, "Job should be added successfully");
    }

    @Test
    public void testGetJobList() {
        Map<String, Object> jobList = xxlJobService.getJobList(0, 10);
        assertNotNull(jobList, "Job list should not be null");
        assertTrue(jobList.containsKey("data"), "Job list should contain 'data' key");
    }

    @Test
    public void testJobTrigger() {
        int jobId = 1;  // Replace with a valid job ID
        boolean result = xxlJobService.triggerJob(jobId, "");
        assertTrue(result, "Job should be triggered successfully");
    }

    // 添加更多测试方法，以验证其他 API 功能
}
