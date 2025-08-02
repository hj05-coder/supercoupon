package com.hj.supercoupon.merchant.admin;


import com.mzt.logapi.starter.annotation.EnableLogRecord;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 商家后管服务 |创建优惠券、店家查看以及管理优惠券、创建优惠券发放批次等
 * @Author web-cat
 */
@SpringBootApplication
@EnableLogRecord(tenant = "MerchantAdmin")
@MapperScan("com.hj.supercoupon.merchant.admin.dao.mapper")
public class MerchantAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(MerchantAdminApplication.class, args);
    }
}
