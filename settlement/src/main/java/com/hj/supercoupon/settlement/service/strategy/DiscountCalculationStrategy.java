package com.hj.supercoupon.settlement.service.strategy;

import com.hj.supercoupon.settlement.dao.entity.CouponTemplateDO;
import com.hj.supercoupon.settlement.dao.entity.DiscountCouponDO;

import java.math.BigDecimal;

/**
 * DiscountCalculationStrategy 实现了 CouponCalculationStrategy 接口，
 * 负责计算折扣类型优惠券的优惠金额。
 * 例如，如果折扣率为 0.8 且订单金额为 100，折扣后金额将为 80。
 */
public class DiscountCalculationStrategy implements CouponCalculationStrategy {

    @Override
    public BigDecimal calculateDiscount(CouponTemplateDO template, BigDecimal orderAmount) {
        DiscountCouponDO discountCoupon = (DiscountCouponDO) template;
        return orderAmount.multiply(BigDecimal.valueOf(discountCoupon.getDiscountRate()));
    }
}
