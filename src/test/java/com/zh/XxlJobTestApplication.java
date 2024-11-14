package com.zh;



import com.zh.annotation.EnableXxlJob;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootApplication
@EnableXxlJob(basePackage = "com.zh")
public class XxlJobTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(XxlJobTestApplication.class, args);
    }
}
