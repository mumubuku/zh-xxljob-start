package com.zh;



import com.zh.annotation.EnableXxlJob;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableXxlJob(basePackage = "com.zh")
public class XxlJobTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(XxlJobTestApplication.class, args);
    }
}
