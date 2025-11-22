package com.hj.supercoupon.settlement.service;


import com.hj.supercoupon.settlement.dao.entity.CouponTemplateDO;
import com.hj.supercoupon.settlement.service.strategy.CouponCalculationStrategy;
import com.hj.supercoupon.settlement.toolkit.CouponFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * CouponCalculationService 是一个用于计算优惠金额的服务类。
 * 它根据不同的优惠券类型，动态选择相应的计算策略，并返回计算后的优惠金额。
 */
@Service
public class CouponCalculationService {

    /**
     * 计算优惠金额。
     * 根据传入的优惠券实例和订单金额，选择相应的计算策略，返回最终的优惠金额。
     *
     * @param coupon      具体的优惠券实例
     * @param orderAmount 订单金额
     * @return 计算出的优惠金额
     */
    public BigDecimal calculateDiscount(CouponTemplateDO coupon, BigDecimal orderAmount) {
        CouponCalculationStrategy strategy = CouponFactory.getCouponCalculationStrategy(coupon);
        return strategy.calculateDiscount(coupon, orderAmount);
    }
}
