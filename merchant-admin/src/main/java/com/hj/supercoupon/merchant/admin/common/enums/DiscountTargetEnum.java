package com.hj.supercoupon.merchant.admin.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 优惠券优惠对象枚举
 */
@RequiredArgsConstructor
public enum DiscountTargetEnum {

    /**
     * 商品专属优惠
     */
    PRODUCT_SPECIFIC(0, "商品专属优惠"),
    /**
     * 全店通用优惠
     */
    ALL_STORE_GENERAL(1, "全店通用优惠");

    @Getter
    private final int type;

    @Getter
    private final String value;

    /**
     * 根据 type 找到对应的 value
     *
     * @param type 要查找的类型代码
     * @return 对应的描述值，如果没有找到抛异常
     */
    public static String findValueByType(int type) {
        for (DiscountTargetEnum target : DiscountTargetEnum.values()) {
            if (target.getType() == type) {
                return target.getValue();
            }
        }
        throw new IllegalArgumentException();
    }
}
