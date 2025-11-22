package com.hj.supercoupon.settlement.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 优惠券模板查询接口返回参数实体
 */
@Data
@Schema(description = "优惠券模板查询返回实体")
public class CouponTemplateQueryRespDTO {

    /**
     * 优惠券id
     */
    @Schema(description = "优惠券id")
    private String id;

    /**
     * 优惠对象 0：商品专属 1：全店通用
     */
    @Schema(description = "优惠对象 0：商品专属 1：全店通用")
    private Integer target;

    /**
     * 优惠商品编码
     */
    @Schema(description = "优惠商品编码")
    private String goods;

    /**
     * 优惠类型 0：立减券 1：满减券 2：折扣券
     */
    @Schema(description = "优惠类型 0：立减券 1：满减券 2：折扣券")
    private Integer type;

    /**
     * 消耗规则
     */
    @Schema(description = "消耗规则")
    private String consumeRule;
}
