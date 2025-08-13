package com.hj.supercoupon.engine.common.constant;

/**
 * 分布式 Redis 缓存引擎层常量类
 */
public final class EngineRedisConstant {

    /**
     * 优惠券模板缓存 Key
     */
    public static final String COUPON_TEMPLATE_KEY = "super-coupon_engine:template:%s";

    /**
     * 优惠券模板缓存分布式锁 Key
     */
    public static final String LOCK_COUPON_TEMPLATE_KEY = "super-coupon_engine:lock:template:%s";
    /**
     * 优惠券模版缓存空值key
     */
    public static final String COUPON_TEMPLATE_IS_NULL_KEY = "super-coupon_engine:template_is_null:%s";
}
