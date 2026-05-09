package com.huanshengxian;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 环生鲜电商平台启动类
 */
@SpringBootApplication
@MapperScan("com.huanshengxian.mapper")
public class HuanShengXianApplication {

    public static void main(String[] args) {
        SpringApplication.run(HuanShengXianApplication.class, args);
    }
}
