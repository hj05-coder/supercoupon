package com.hj.supercoupon.merchant.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hj.supercoupon.framework.idempotent.NoDuplicateSubmit;
import com.hj.supercoupon.framework.result.Result;
import com.hj.supercoupon.framework.web.Results;
import com.hj.supercoupon.merchant.admin.dto.req.CouponTemplateNumberReqDTO;
import com.hj.supercoupon.merchant.admin.dto.req.CouponTemplatePageQueryReqDTO;
import com.hj.supercoupon.merchant.admin.dto.req.CouponTemplateSaveReqDTO;
import com.hj.supercoupon.merchant.admin.dto.resp.CouponTemplatePageQueryRespDTO;
import com.hj.supercoupon.merchant.admin.dto.resp.CouponTemplateQueryRespDTO;
import com.hj.supercoupon.merchant.admin.service.CouponTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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
    @NoDuplicateSubmit
    @Operation(summary = "商家创建优惠券模版")
    @PostMapping("/api/merchant-admin/coupon-template/create")
    public Result<Void> createCouponTemplate(@RequestBody CouponTemplateSaveReqDTO couponTemplateSaveReqDTO) {
        couponTemplateService.createCouponTemplate(couponTemplateSaveReqDTO);
        return Results.success();
    }
    @Operation(summary = "分页查询优惠券模板")
    @GetMapping("/api/merchant-admin/coupon-template/page")
    public Result<IPage<CouponTemplatePageQueryRespDTO>> pageQueryCouponTemplate(CouponTemplatePageQueryReqDTO requestParam) {
        return Results.success(couponTemplateService.pageQueryCouponTemplate(requestParam));
    }
    @Operation(summary = "查询优惠券模板详情")
    @GetMapping("/api/merchant-admin/coupon-template/find")
    public Result<CouponTemplateQueryRespDTO> findCouponTemplate(String couponTemplateId) {
        return Results.success(couponTemplateService.findCouponTemplateById(couponTemplateId));
    }

    @Operation(summary = "增加优惠券模板发行量")
    @NoDuplicateSubmit(message = "请勿短时间内重复增加优惠券发行量")
    @PostMapping("/api/merchant-admin/coupon-template/increase-number")
    public Result<Void> increaseNumberCouponTemplate(@RequestBody CouponTemplateNumberReqDTO requestParam) {
        couponTemplateService.increaseNumberCouponTemplate(requestParam);
        return Results.success();
    }

    @Operation(summary = "结束优惠券模板")
    @PostMapping("/api/merchant-admin/coupon-template/terminate")
    public Result<Void> terminateCouponTemplate(String couponTemplateId) {
        couponTemplateService.terminateCouponTemplate(couponTemplateId);
        return Results.success();
    }
}
