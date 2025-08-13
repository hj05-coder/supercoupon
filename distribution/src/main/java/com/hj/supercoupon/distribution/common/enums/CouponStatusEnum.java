package com.hj.supercoupon.distribution.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 优惠券使用状态枚举类
 */
@RequiredArgsConstructor
public enum CouponStatusEnum {

    /**
     * 生效中
     */
    EFFECTIVE(0),

    /**
     * 已结束
     */
    ENDED(1);

    @Getter
    private final int type;
}
