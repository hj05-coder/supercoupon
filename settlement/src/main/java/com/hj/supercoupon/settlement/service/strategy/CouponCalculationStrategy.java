package com.hj.supercoupon.settlement.service.strategy;

import com.hj.supercoupon.settlement.dao.entity.CouponTemplateDO;

import java.math.BigDecimal;

/**
 * CouponCalculationStrategy 是一个用于计算优惠券折扣金额的策略接口。
 * 各种类型的优惠券（如折扣券、满减券等）可以通过实现该接口来定义其具体的折扣计算逻辑。
 */
public interface CouponCalculationStrategy {

    /**
     * 计算折扣
     *
     * @param template    优惠券模板
     * @param orderAmount 订单金额
     * @return 优惠后金额
     */
    BigDecimal calculateDiscount(CouponTemplateDO template, BigDecimal orderAmount);
}
