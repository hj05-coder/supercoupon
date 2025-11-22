package com.hj.supercoupon.settlement.dao.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("t_coupon_settlement")
public class CouponSettlementDO {

    /**
     * 结算单ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 店铺编号
     */
    private String shopNumber;

    /**
     * 优惠券ID
     */
    private Long couponId;

    /**
     * 结算单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 应付金额
     */
    private BigDecimal payableAmount;

    /**
     * 券金额
     */
    private BigDecimal couponAmount;

    /**
     * 结算单状态
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
