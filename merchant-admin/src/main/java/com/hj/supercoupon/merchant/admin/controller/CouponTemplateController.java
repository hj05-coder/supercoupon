package com.hj.supercoupon.merchant.admin.controller;

import com.hj.supercoupon.framework.result.Result;
import com.hj.supercoupon.framework.web.Results;
import com.hj.supercoupon.merchant.admin.dto.req.CouponTemplateSaveReqDTO;
import com.hj.supercoupon.merchant.admin.service.CouponTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 优惠券模版控制层
 * @Author web-cat
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "优惠券模版管理")
public class CouponTemplateController {
    private final CouponTemplateService couponTemplateService;
    @Operation(summary = "商家创建优惠券模版")
    @PostMapping("/api/merchant-admin/coupon-template/create")
    public Result<Void> createCouponTemplate(@RequestBody CouponTemplateSaveReqDTO couponTemplateSaveReqDTO) {
        couponTemplateService.createCouponTemplate(couponTemplateSaveReqDTO);
        return Results.success();
    }
}
