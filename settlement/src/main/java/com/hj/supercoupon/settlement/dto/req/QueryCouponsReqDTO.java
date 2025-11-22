package com.hj.supercoupon.settlement.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 查询用户优惠券请求参数
 */
@Data
@Schema(description = "查询用户优惠券请求参数")
public class QueryCouponsReqDTO {

    /**
     * 订单金额
     */
    @Schema(description = "订单金额", required = true)
    private BigDecimal orderAmount;

    /**
     * 店铺编号
     */
    @Schema(description = "店铺编号", example = "1810714735922956666", required = true)
    private String shopNumber;

    /**
     * 商品集合
     */
    @Schema(description = "商品集合", required = true)
    private List<QueryCouponGoodsReqDTO> goodsList;
}
