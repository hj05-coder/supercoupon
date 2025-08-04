package com.hj.supercoupon.merchant.admin.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 优惠券推送任务发送类型枚举
 */
@RequiredArgsConstructor
public enum CouponTaskSendTypeEnum {

    /**
     * 立即发送
     */
    IMMEDIATE(0),

    /**
     * 定时发送
     */
    SCHEDULED(1);

    @Getter
    private final int type;
}
