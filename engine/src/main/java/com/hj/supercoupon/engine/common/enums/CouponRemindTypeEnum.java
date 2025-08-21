package com.hj.supercoupon.engine.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 预约提醒方式枚举类，值必须是0，1，2，3......
 */
@RequiredArgsConstructor
public enum CouponRemindTypeEnum {

    /**
     * App 通知
     */
    APP(0, "App通知"),

    /**
     * 邮件提醒
     */
    EMAIL(1, "邮件提醒");

    @Getter
    private final int type;
    @Getter
    private final String describe;

    public static CouponRemindTypeEnum getByType(Integer type) {
        for (CouponRemindTypeEnum remindEnum : values()) {
            if (remindEnum.getType() == type) {
                return remindEnum;
            }
        }
        return null;
    }

    public static String getDescribeByType(Integer type) {
        for (CouponRemindTypeEnum remindEnum : values()) {
            if (remindEnum.getType() == type) {
                return remindEnum.getDescribe();
            }
        }
        return null;
    }
}
