package com.zh.handler;

import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import com.xxl.job.core.biz.model.ReturnT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.CompletableFuture;

/**
 * BaseJobHandler 是一个任务执行模板类，用于封装通用的任务处理逻辑。
 * 子类可以继承该类，实现具体的业务逻辑。
 * @author mumu
 */
public abstract class BaseJobHandler extends IJobHandler {

    private static final Logger logger = LoggerFactory.getLogger(BaseJobHandler.class);

    private final boolean async; // 是否异步执行任务
    private final int maxRetries; // 最大重试次数

    /**
     * 构造函数
     * @param async 是否异步执行任务
     * @param maxRetries 最大重试次数
     */
    public BaseJobHandler(boolean async, int maxRetries) {
        this.async = async;
        this.maxRetries = maxRetries;
    }

    /**
     * 接收字符串参数并返回任务执行结果
     */
    @Override
    public ReturnT<String> execute(String param) throws Exception {
        if (async) {
            // 异步执行任务
            CompletableFuture<ReturnT<String>> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return executeWithRetries(param);
                } catch (Exception e) {
                    handleException(e);
                    return ReturnT.FAIL;
                }
            });
            return future.get(); // 返回异步结果
        } else {
            // 同步执行任务
            return executeWithRetries(param);
        }
    }

    /**
     * 使用重试机制执行任务
     */
    private ReturnT<String> executeWithRetries(String param) throws Exception {
        int attempt = 0;
        Exception lastException = null;
        while (attempt < maxRetries) {
            try {
                beforeExecution(param); // 前置操作
                ReturnT<String> result = doExecute(param); // 执行具体任务逻辑
                afterExecution(param); // 后置操作
                return result;
            } catch (Exception e) {
                lastException = e;
                attempt++;
                if (attempt < maxRetries) {
                    XxlJobLogger.log("执行失败，第 " + attempt + " 次重试中...");
                    logger.warn("任务执行失败，第 {} 次重试中...", attempt, e);
                }
            }
        }
        if (lastException != null) {
            throw lastException; // 如果重试次数用尽，抛出最后一次异常
        }
        return ReturnT.FAIL;
    }

    /**
     * 任务执行的具体逻辑，子类需要实现该方法
     */
    protected abstract ReturnT<String> doExecute(String param) throws Exception;

    /**
     * 前置操作，可以被子类覆盖实现
     */
    protected void beforeExecution(String param) {
        XxlJobLogger.log("任务开始执行, 参数: " + param);
    }

    /**
     * 后置操作，可以被子类覆盖实现
     */
    protected void afterExecution(String param) {
        XxlJobLogger.log("任务执行完成, 参数: " + param);
    }

    /**
     * 异常处理
     * @param e 异常信息
     */
    protected void handleException(Exception e) {
        logger.error("任务执行异常", e);
        XxlJobLogger.log("任务执行异常: " + e.getMessage());
    }

    /**
     * 验证任务参数，子类可以覆盖该方法实现自定义验证
     */
    protected void validateParams(String param) throws IllegalArgumentException {
        // 默认实现为空
    }
}
