package com.hj.supercoupon.merchant.admin.template;

import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson2.JSONObject;

import com.hj.supercoupon.merchant.admin.common.enums.CouponTemplateStatusEnum;
import com.hj.supercoupon.merchant.admin.dao.entity.CouponTemplateDO;
import com.hj.supercoupon.merchant.admin.service.CouponTemplateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Date;

@SpringBootTest
public class CouponTemplateTest {

    @Autowired
    private CouponTemplateService couponTemplateService;

    public CouponTemplateDO buildCouponTemplateDO() {
        JSONObject receiveRule = new JSONObject();
        receiveRule.put("limitPerPerson", 1); // 每人限领
        receiveRule.put("usageInstructions", "使用说明"); // 使用说明
        JSONObject consumeRule = new JSONObject();
        consumeRule.put("termsOfUse", new BigDecimal("10")); // 使用条件 满 x 元可用
        consumeRule.put("maximumDiscountAmount", new BigDecimal("3")); // 最大优惠金额
        consumeRule.put("explanationOfUnmetC 3onditions", "不满足使用条件说明"); // 不满足使用条件说明
        consumeRule.put("validityPeriod", 48); // 自领取优惠券后有效时间，单位小时
        CouponTemplateDO couponTemplateDO = CouponTemplateDO.builder()
                .shopNumber(1810714735922956666L) // 店铺编号
                .name("商品立减券") // 优惠券名称
                .source(0) // 优惠券来源 0：店铺券 1：平台券
                .target(1) // 优惠对象 0：商品专属 1：全店通用
                .type(0) // 优惠类型 0：立减券 1：满减券 2：折扣券
                .validStartTime(new Date()) // 有效期开始时间
                .validEndTime(new Date()) // 有效期结束时间
                .stock(10) // 库存
                .receiveRule(receiveRule.toString()) // 领取规则
                .consumeRule(consumeRule.toString()) // 消耗规则
                .status(CouponTemplateStatusEnum.ACTIVE.getStatus()) // 优惠券状态 0：生效中 1：已结束
                .build();
        return couponTemplateDO;
    }

    /**
     * 测试新增优惠券模板方法
     */
    @Test
    public void testInsertCouponTemplate() {
        boolean saved = couponTemplateService.save(buildCouponTemplateDO());
        Assert.isTrue(saved);
    }
}
