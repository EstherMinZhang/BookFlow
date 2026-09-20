package com.bookflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BookflowApplication {

    // 应用入口：启动 Spring Boot 后端服务，并自动扫描 com.bookflow 包下的组件。
    public static void main(String[] args) {
        SpringApplication.run(BookflowApplication.class, args);
    }
}
