package com.hj.supercoupon.engine.service;
import com.hj.supercoupon.engine.dto.req.CouponCreatePaymentReqDTO;
import com.hj.supercoupon.engine.dto.req.CouponProcessPaymentReqDTO;
import com.hj.supercoupon.engine.dto.req.CouponProcessRefundReqDTO;
import com.hj.supercoupon.engine.dto.req.CouponTemplateRedeemReqDTO;

/**
 * 用户优惠券业务逻辑层
 */
public interface UserCouponService {

    /**
     * 用户兑换优惠券
     *
     * @param requestParam 请求参数
     */
    void redeemUserCoupon(CouponTemplateRedeemReqDTO requestParam);

    /**
     * 用户兑换优惠券消息队列版本
     * 支持更高的并发，完全依赖缓存做前置校验，如果缓存认为没问题，直接返回用户请求成功，在消息队列中做扣减等一系列流程
     *
     * @param requestParam 请求参数
     */
    void redeemUserCouponByMQ(CouponTemplateRedeemReqDTO requestParam);

    /**
     * 创建优惠券结算单记录
     *
     * @param requestParam 创建优惠券结算单请求参数
     */
    void createPaymentRecord(CouponCreatePaymentReqDTO requestParam);

    /**
     * 处理订单支付操作，修改结算单为已支付
     *
     * @param requestParam 处理优惠券结算单请求参数
     */
    void processPayment(CouponProcessPaymentReqDTO requestParam);

    /**
     * 处理订单退款操作，修改结算单为已退款并回滚优惠券
     *
     * @param requestParam 处理优惠券结算单退款请求参数
     */
    void processRefund(CouponProcessRefundReqDTO requestParam);
}
