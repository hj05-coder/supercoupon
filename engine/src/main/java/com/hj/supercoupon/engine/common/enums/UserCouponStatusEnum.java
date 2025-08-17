package com.hj.supercoupon.engine.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 用户优惠券状态枚举
 */
@RequiredArgsConstructor
public enum UserCouponStatusEnum {

    /**
     * 未使用
     */
    UNUSED(0),

    /**
     * 锁定
     */
    LOCKING(1),

    /**
     * 已使用
     */
    USED(2),

    /**
     * 已过期
     */
    EXPIRED(3),

    /**
     * 已撤回
     */
    REVOKED(4);

    @Getter
    private final int code;
}
