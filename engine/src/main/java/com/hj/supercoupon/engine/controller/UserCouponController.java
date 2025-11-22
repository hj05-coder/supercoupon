package com.hj.supercoupon.engine.controller;

import com.hj.supercoupon.engine.dto.req.CouponCreatePaymentReqDTO;
import com.hj.supercoupon.engine.dto.req.CouponProcessPaymentReqDTO;
import com.hj.supercoupon.engine.dto.req.CouponProcessRefundReqDTO;
import com.hj.supercoupon.engine.dto.req.CouponTemplateRedeemReqDTO;
import com.hj.supercoupon.engine.service.UserCouponService;
import com.hj.supercoupon.framework.result.Result;
import com.hj.supercoupon.framework.web.Results;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户优惠券控制层
 * @author web-cat
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "用户优惠券管理")
public class UserCouponController {
    private final UserCouponService userCouponService;

    @Operation(summary = "兑换优惠券模版", description = "存在较高流量场景,可类比秒杀业务")
    @PostMapping("/api/engine/user-coupon/redeem")
    public Result<Void> redeemUserCoupon(@RequestBody CouponTemplateRedeemReqDTO requestParam){
        userCouponService.redeemUserCoupon(requestParam);
        return Results.success();
    }
    @Operation(summary = "兑换优惠券模版之消息队列", description = "存在较高流量场景,可对比秒杀业务")
    @PostMapping("/api/engine/user-coupon/redeem-mq")
    public Result<Void> redeemUserCouponByMQ(@RequestBody CouponTemplateRedeemReqDTO requestParam){
        userCouponService.redeemUserCouponByMQ(requestParam);
        return Results.success();
    }

    @Operation(summary = "创建用户优惠券结算单", description = "用户下单时锁定使用的优惠券, 一般由订单系统发起调用")
    @PostMapping("/api/engine/user-coupon/create-payment-record")
    public Result<Void> createPaymentRecord(@RequestBody CouponCreatePaymentReqDTO requestParam){
        userCouponService.createPaymentRecord(requestParam);
        return Results.success();
    }

    @Operation(summary = "核销优惠券结算单", description = "用户支付后核销使用的优惠券, 常过来说应该监听支付后的消息队列事件")
    @PostMapping("/api/engine/user-coupon/process-payment")
    public Result<Void> processPayment(@RequestBody CouponProcessPaymentReqDTO requestParam) {
        userCouponService.processPayment(requestParam);
        return Results.success();
    }

    @Operation(summary = "退款优惠券结算单", description = "用户退款时处理使用的优惠券, 常过来说应该监听退款后的消息队列事件")
    @PostMapping("/api/engine/user-coupon/process-refund")
    public Result<Void> processRefund(@RequestBody CouponProcessRefundReqDTO requestParam){
        userCouponService.processRefund(requestParam);
        return Results.success();
    }
}
