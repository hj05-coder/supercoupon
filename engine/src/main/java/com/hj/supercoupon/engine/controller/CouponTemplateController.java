package com.hj.supercoupon.engine.controller;

import com.hj.supercoupon.engine.dto.req.CouponTemplateQueryReqDTO;
import com.hj.supercoupon.engine.dto.resp.CouponTemplateQueryRespDTO;
import com.hj.supercoupon.engine.service.CouponTemplateService;
import com.hj.supercoupon.framework.result.Result;
import com.hj.supercoupon.framework.web.Results;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 优惠券模版控制层
 * @author web-cat
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "优惠券模板管理")
public class CouponTemplateController {

    private final CouponTemplateService couponTemplateService;
    @Operation(summary = "查询优惠券模板")
    @GetMapping("/api/engine/coupon-template/query")
    public Result<CouponTemplateQueryRespDTO> findCouponTemplate(CouponTemplateQueryReqDTO requestParam) {
        return Results.success(couponTemplateService.findCouponTemplate(requestParam));
    }
}
