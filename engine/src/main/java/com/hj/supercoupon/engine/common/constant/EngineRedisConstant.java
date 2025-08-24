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
    /**
     * 限制用户领取优惠券模板次数缓存 Key
     */
    public static final String USER_COUPON_TEMPLATE_LIMIT_KEY = "super-coupon_engine:user-template-limit:%s_%s";

    /**
     * 用户已领取优惠券列表模板 Key
     */
    public static final String USER_COUPON_TEMPLATE_LIST_KEY = "super-coupon_engine:user-template-list:%s";
    /**
     * 检查用户是否已提醒 Key
     */
    public static final String COUPON_REMIND_CHECK_KEY = "super-coupon_engine:coupon-remind-check:%s_%s_%d_%d";
    /**
     * 用户预约提醒信息 Key
     */
    public static final String USER_COUPON_TEMPLATE_REMIND_INFORMATION = "super-coupon_engine:coupon-remind-information:%s";
    /**
     * 优惠券结算单分布式锁 Key
     */
    public static final String LOCK_COUPON_SETTLEMENT_KEY = "super-coupon_engine:lock:coupon-settlement:%d";
}
