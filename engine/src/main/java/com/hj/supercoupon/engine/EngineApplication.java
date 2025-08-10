package com.hj.supercoupon.engine;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 引擎服务 | 负责优惠券单个查看、列表查看、锁定及核销等功能
 * @Author web-cat
 */
@SpringBootApplication
@MapperScan("com.hj.supercoupon.engine.dao.mapper")
public class EngineApplication {
    public static void main(String[] args) {
        SpringApplication.run(EngineApplication.class, args);
    }
}
