package com.zh.service;

import com.xxl.job.core.handler.annotation.XxlJob;
import com.zh.annotation.XxlJobTask;
import org.springframework.stereotype.Component;

/**
 * @BelongsProject: zh-xxljob-starter
 * @BelongsPackage: com.zh.service
 * @Author: mumu
 * @CreateTime: 2024-11-14  11:02
 * @Description: TODO
 * @Version: 1.0
 */

@Component
public class XXLJobTaskTest {


    @XxlJobTask(jobDesc = "测试任务运行2")
    @XxlJob("test")
    public void  test() {

        System.out.println("测试输出");
    }
}
