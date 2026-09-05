package com.retail;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 智能零售客服与订单协同平台 - 启动类
 *
 * @author retail-platform
 */
@SpringBootApplication
@EnableScheduling
@MapperScan("com.retail.mapper")
public class RetailApplication {

    public static void main(String[] args) {
        SpringApplication.run(RetailApplication.class, args);
    }
}
