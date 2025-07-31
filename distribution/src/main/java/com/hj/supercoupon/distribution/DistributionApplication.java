package com.hj.supercoupon.distribution;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 分发模块 |负责按批次分发用户优惠券,可提供应用弹框推送、站内信或短信通知等
 * @Author web-cat
 */
@SpringBootApplication
public class DistributionApplication {
    public static void main(String[] args) {
        SpringApplication.run(DistributionApplication.class, args);
    }
}
