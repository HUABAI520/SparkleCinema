package com.cuit.gcsj;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author L
 */
@MapperScan("com.cuit.gcsj.mapper")
@SpringBootApplication
@EnableScheduling  // 开启对定时任务的支持
public class SparkleCinemaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SparkleCinemaApplication.class, args);
    }
}
