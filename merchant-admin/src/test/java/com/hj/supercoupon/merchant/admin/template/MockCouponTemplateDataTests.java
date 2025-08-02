package com.hj.supercoupon.merchant.admin.template;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.RandomUtil;

import com.hj.supercoupon.merchant.admin.dao.entity.CouponTemplateDO;
import com.hj.supercoupon.merchant.admin.dao.mapper.CouponTemplateMapper;
import jodd.util.ThreadUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Mock 优惠券模板数据，方便分库分表均衡测试
 */
@SpringBootTest
public class MockCouponTemplateDataTests {

    @Autowired
    private CouponTemplateMapper couponTemplateMapper;

    private final CouponTemplateTest couponTemplateTest = new CouponTemplateTest();
    private final List<Snowflake> snowflakes = new ArrayList<>();
    private final ExecutorService executorService = new ThreadPoolExecutor(
            10,
            10,
            9999,
            TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );
    private final int maxNum = 50000;

    public void beforeDataBuild() {
        for (int i = 0; i < 20; i++) {
            snowflakes.add(new Snowflake(i));
        }
    }

    @Test
    public void mockCouponTemplateTest() {
        beforeDataBuild();
        AtomicInteger count = new AtomicInteger(0);
        while (count.get() < maxNum) {
            executorService.execute(() -> {
                ThreadUtil.sleep(RandomUtil.randomInt(10));
                CouponTemplateDO couponTemplateDO = couponTemplateTest.buildCouponTemplateDO();
                couponTemplateDO.setShopNumber(snowflakes.get(RandomUtil.randomInt(20)).nextId());
                couponTemplateMapper.insert(couponTemplateDO);
                count.incrementAndGet();
            });
        }
    }
}
