package com.zh.annotation;



import com.zh.config.XXLJobTaskRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(XXLJobTaskRegistrar.class)  // 导入任务注册类
public @interface EnableXxlJob {
    /**
     * 指定扫描任务的包路径
     */
    String basePackage();
}
