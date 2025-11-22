package com.hj.supercoupon.settlement.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 查询用户优惠券响应参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "查询用户优惠券响应参数")
public class QueryCouponsRespDTO {

    /**
     * 可用优惠券列表
     */
    @Schema(description = "可用优惠券列表")
    private List<QueryCouponsDetailRespDTO> availableCouponList;

    /**
     * 不可用优惠券列表
     */
    @Schema(description = "不可用优惠券列表")
    private List<QueryCouponsDetailRespDTO> notAvailableCouponList;
}
