package com.workorder;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.workorder.mapper")
@EnableScheduling   // 启用定时任务（SLA 监控）
@EnableAsync        // 启用异步支持（可选，用于发送消息）
public class WorkOrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorkOrderApplication.class, args);
        System.out.println("========================================");
        System.out.println("  企业工单系统启动成功！");
        System.out.println("  接口文档：http://localhost:8080/doc.html");
        System.out.println("========================================");
    }
}