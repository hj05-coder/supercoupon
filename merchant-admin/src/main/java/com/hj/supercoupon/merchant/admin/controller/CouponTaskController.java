package com.hj.supercoupon.merchant.admin.controller;

import com.hj.supercoupon.framework.idempotent.NoDuplicateSubmit;
import com.hj.supercoupon.framework.result.Result;
import com.hj.supercoupon.framework.web.Results;
import com.hj.supercoupon.merchant.admin.dto.req.CouponTaskCreateReqDTO;
import com.hj.supercoupon.merchant.admin.service.CouponTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 优惠券推送任务控制层
 * @author web-cat
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "优惠券推送任务管理")
public class CouponTaskController {

    private final CouponTaskService couponTaskService;
    @Operation(summary = "创建优惠券推送任务")
    @NoDuplicateSubmit(message = "请勿短时间内重复提交优惠券推送任务")
    @PostMapping("/api/merchant-admin/coupon-task/create")
    public Result<Void> createCouponTask(@RequestBody CouponTaskCreateReqDTO requestParam) {
        couponTaskService.createCouponTask(requestParam);
        return Results.success();
    }
}
