package com.hj.supercoupon.engine.toolkit;

/**
 * 扣减优惠券模板库存复合返回工具类
 */
public final class StockDecrementReturnCombinedUtil {

    /**
     * 2^14 > 9999, 所以用 14 位来表示第二个字段
     */
    private static final int SECOND_FIELD_BITS = 14;

    /**
     * 从组合的 int 中提取第一个字段（0、1或2）
     */
    public static long extractFirstField(long combined) {
        return (combined >> SECOND_FIELD_BITS) & 0b11; // 0b11 即二进制的 11，用于限制结果为 2 位
    }

    /**
     * 从组合的 int 中提取第二个字段（0 到 9999 之间的数字）
     */
    public static long extractSecondField(long combined) {
        return combined & ((1 << SECOND_FIELD_BITS) - 1);
    }
}
