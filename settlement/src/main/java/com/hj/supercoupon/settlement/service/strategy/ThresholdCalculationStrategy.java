package com.hj.supercoupon.settlement.service.strategy;

import com.hj.supercoupon.settlement.dao.entity.CouponTemplateDO;
import com.hj.supercoupon.settlement.dao.entity.ThresholdCouponDO;

import java.math.BigDecimal;

/**
 * 该类用于计算有门槛固定金额优惠券的折扣金额。若满足门槛要求，该优惠券在结算时会直接减去固定的折扣金额
 * 例如，如果优惠券的折扣金额为 50 元，门槛为100 元，满足门槛的订单 使用该优惠券后，订单金额会减少 50 元。
 */
public class ThresholdCalculationStrategy implements CouponCalculationStrategy {

    @Override
    public BigDecimal calculateDiscount(CouponTemplateDO template, BigDecimal orderAmount) {
        ThresholdCouponDO thresholdDiscount = (ThresholdCouponDO) template;
        if (orderAmount.compareTo(BigDecimal.valueOf(thresholdDiscount.getThresholdAmount())) >= 0) {
            return BigDecimal.valueOf(thresholdDiscount.getDiscountAmount());
        }
        return BigDecimal.ZERO;
    }
}
