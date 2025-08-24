package com.hj.supercoupon.engine.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建优惠券结算单商品请求参数实体
 */
@Data
public class CouponCreatePaymentGoodsReqDTO {

    /**
     * 商品编号
     */
    @Schema(description = "商品编号")
    private String goodsNumber;

    /**
     * 商品价格
     */
    @Schema(description = "商品价格")
    private BigDecimal goodsAmount;

    /**
     * 商品折扣后金额
     */
    @Schema(description = "商品折扣后金额")
    private BigDecimal goodsPayableAmount;
}
