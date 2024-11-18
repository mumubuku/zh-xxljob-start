
---

# XXL-Job Starter

这是一个封装的 `xxl-job` starter，用于简化 XXL-Job 在 Spring Boot 项目中的集成。它支持任务的自动注册、自定义任务执行模板、重试机制、延迟任务等功能，并集成了 Redisson 进行分布式锁管理，确保任务在分布式环境中的幂等性。

## 功能特性

- 支持任务参数验证
- 支持重试机制
- 支持延迟执行
- 支持通过注解配置任务
- 支持一次性任务的执行

## 目录

- [安装和配置](#安装和配置)
- [快速开始](#快速开始)
- [任务注解参数说明](#任务注解参数说明)
- [任务执行模板](#任务执行模板)
- [高级功能](#高级功能)
- [测试](#测试)

---

## 安装和配置

1. **在 Maven 项目中引入依赖**：

    ```xml
    <dependency>
        <groupId>com.zh</groupId>
        <artifactId>xxl-job-starter</artifactId>
        <version>1.0.0</version>
    </dependency>
    ```

2. **配置 `application.yml` 文件**：

    ```yaml
    xxl:
      job:
        admin-addresses: http://localhost:8080/xxl-job-admin  # XXL-JOB 管理中心地址
        access-token: YOUR_ACCESS_TOKEN                       # 访问令牌，可选
        appname: xxl-job-app                                  # 应用名称
        address: YOUR_IP_ADDRESS                              # 当前节点 IP，可选
        logpath: /data/applogs/xxl-job/jobhandler             # 日志路径
        username: admin                                       # 管理后台用户名（使用 session 模式）
        password: password                                    # 管理后台密码（使用 session 模式）
    ```

3. **添加主启动类注解**：

    ```java
    import com.zh.annotation.EnableXxlJob;

    @SpringBootApplication
    @EnableXxlJob(basePackage = "com.your.package.jobs")
    public class XxlJobApplication {
        public static void main(String[] args) {
            SpringApplication.run(XxlJobApplication.class, args);
        }
    }
    ```

## 快速开始

### 1. 定义任务

使用 `@XxlJobTask` 注解来定义任务：

```java
import com.zh.annotation.XxlJobTask;
import com.xxl.job.core.biz.model.ReturnT;

@Component
public class SampleJob {

    @XxlJobTask(
        jobDesc = "示例任务",
        cron = "0/5 * * * * ?", // 每 5 秒执行一次
        executorHandler = "sampleJobHandler",
        author = "admin"
    )
    public ReturnT<String> executeJob(String param) {
        // 任务执行逻辑
        System.out.println("任务执行，参数：" + param);
        return ReturnT.SUCCESS;
    }
}
```

### 2. 注册和执行任务

在启动应用时，`XXLJobTaskRegistrar` 会自动扫描并注册使用 `@XxlJobTask` 注解的任务到 XXL-JOB 管理中心。可以在管理中心手动执行任务，也可以根据 `cron` 表达式自动执行。

## 任务注解参数说明

| 参数                   | 描述                           | 默认值           |
|------------------------|--------------------------------|------------------|
| `jobDesc`              | 任务描述                       | 必须配置         |
| `cron`                 | CRON 表达式                    | 无               |
| `executorHandler`      | 执行器 Handler 名称            | 必须配置         |
| `author`               | 任务作者                       | 无               |
| `alarmEmail`           | 报警邮件                       | 无               |
| `executorRouteStrategy`| 路由策略（如 FIRST、LAST）     | FIRST           |
| `misfireStrategy`      | 错过任务的处理策略             | DO_NOTHING      |
| `executorBlockStrategy`| 阻塞处理策略                   | SERIAL_EXECUTION|
| `executorTimeout`      | 任务执行超时时间（秒）         | 0（不限时）     |
| `executorFailRetryCount`| 失败重试次数                  | 0               |

## 任务执行模板

`BaseJobHandler` 提供了通用任务执行模板。自定义任务只需继承 `BaseJobHandler` 并实现 `doExecute(String param)` 方法。

**示例**：

```java
import com.zh.template.BaseJobHandler;
import com.xxl.job.core.biz.model.ReturnT;
import org.springframework.stereotype.Component;

@Component("myJobHandler")
public class MyJobHandler extends BaseJobHandler {

    public MyJobHandler() {
        super(false, 3); // 同步执行，重试 3 次
    }

    @Override
    protected ReturnT<String> doExecute(String param) throws Exception {
        // 执行任务逻辑
        return ReturnT.SUCCESS;
    }
}
```

## 高级功能

- **延迟任务**：使用 `@XxlJobTask(delay = 5000)` 实现任务的延迟执行。
- **任务分组**：可以在注解上配置 `jobGroup` 来指定任务的分组。
- **条件触发**：基于 Spring 条件注解，可以使用 `@ConditionalOnExpression` 等条件来控制任务执行条件。

## 测试

在测试项目中模拟配置：

1. **创建测试类**：

    ```java
    @SpringBootTest
    public class XxlJobTest {

        @Autowired
        private XxlJobService xxlJobService;

        @Test
        public void testAddJob() {
            Map<String, Object> jobConfig = new HashMap<>();
            jobConfig.put("jobGroup", 1);
            jobConfig.put("jobDesc", "测试任务");
            jobConfig.put("executorHandler", "testHandler");
            jobConfig.put("scheduleType", "CRON");
            jobConfig.put("scheduleConf", "0 0/1 * * * ?");
            boolean result = xxlJobService.addJob(jobConfig);
            Assertions.assertTrue(result);
        }
    }
    ```

2. **运行测试**：

   运行测试，验证任务是否正确添加和执行。

---

这样，通过简单的配置和注解即可将 XXL-JOB 集成到您的项目中，支持任务的自动注册、参数验证、分布式锁和任务执行模板等功能。
