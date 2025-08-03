package com.hj.supercoupon.merchant.admin.template.parallel;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import com.hj.supercoupon.merchant.admin.dao.entity.CouponTemplateDO;
import com.hj.supercoupon.merchant.admin.dao.mapper.CouponTemplateMapper;
import com.hj.supercoupon.merchant.admin.template.CouponTemplateTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 并行增加优惠券库存单元测试
 */
@SpringBootTest
public class CouponTemplateConcurrentInCreaseNumberTests {

    @Autowired
    private CouponTemplateMapper couponTemplateMapper;

    private CouponTemplateTest couponTemplateTest;
    private CouponTemplateDO couponTemplateDO;

    @BeforeEach
    public void setUp() {
        couponTemplateTest = new CouponTemplateTest();
        couponTemplateDO = couponTemplateTest.buildCouponTemplateDO();
        couponTemplateMapper.insert(couponTemplateDO);
    }

    @Test
    public void testConcurrentIncreaseNumber() throws InterruptedException {
        int threadCount = 200;
        int increaseAmount = 10;
        long shopNumber = couponTemplateDO.getShopNumber();
        long couponTemplateId = couponTemplateDO.getId();

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                couponTemplateMapper.increaseNumberCouponTemplate(shopNumber, String.valueOf(couponTemplateId), increaseAmount);
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        LambdaQueryWrapper<CouponTemplateDO> queryWrapper = Wrappers.lambdaQuery(CouponTemplateDO.class)
                .eq(CouponTemplateDO::getShopNumber, shopNumber)
                .eq(CouponTemplateDO::getId, couponTemplateDO.getId());
        CouponTemplateDO updatedCouponTemplateDO = couponTemplateMapper.selectOne(queryWrapper);

        int expectedNumber = couponTemplateDO.getStock() + (threadCount * increaseAmount);
        assertEquals(expectedNumber, updatedCouponTemplateDO.getStock(), "The stock count should match the expected value.");
    }
}