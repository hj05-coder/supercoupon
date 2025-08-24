package com.hj.supercoupon.engine.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 处理优惠券结算单退款请求参数实体
 */
@Data
public class CouponProcessRefundReqDTO {

    /**
     * 优惠券ID
     */
    @Schema(description = "优惠券ID", required = true)
    private Long couponId;
}
