package com.hj.supercoupon.settlement.dao.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 折扣券数据库持久层实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountCouponDO extends CouponTemplateDO {

    private Double discountRate;

    @Builder(builderMethodName = "discountCouponBuilder")
    public DiscountCouponDO(CouponTemplateDO coupon, Double discountRate) {
        super(coupon.getId(), coupon.getShopNumber(), coupon.getName(), coupon.getSource(), coupon.getTarget(), coupon.getGoods(), coupon.getType(),
                coupon.getValidStartTime(), coupon.getValidEndTime(), coupon.getStock(), coupon.getReceiveRule(), coupon.getConsumeRule(), coupon.getStatus(),
                coupon.getCreateTime(), coupon.getUpdateTime(), coupon.getDelFlag());
        this.discountRate = discountRate;
    }

    public DiscountCouponDO(Long id, Long shopNumber, String name, Integer source, Integer target, String goods, Integer type, Date validStartTime, Date validEndTime, Integer stock, String receiveRule, String consumeRule, Integer status, Date createTime, Date updateTime, Integer delFlag, Double discountRate) {
        super(id, shopNumber, name, source, target, goods, type, validStartTime, validEndTime, stock, receiveRule, consumeRule, status, createTime, updateTime, delFlag);
        this.discountRate = discountRate;
    }

    /**
     * 静态方法，用于返回新的 Builder 实例
     */
    public static DiscountCouponDOBuilder builder() {
        return new DiscountCouponDOBuilder();
    }

    /**
     * 静态内部类，用于返回新的 Builder 实例
     */
    public static class DiscountCouponDOBuilder extends CouponTemplateDO.CouponTemplateDOBuilder {
        private Double discountRate;

        DiscountCouponDOBuilder() {
            super();
        }

        public DiscountCouponDOBuilder discountRate(Double discountRate) {
            this.discountRate = discountRate;
            return this;
        }

        @Override
        public DiscountCouponDO build() {
            CouponTemplateDO coupon = super.build();
            return new DiscountCouponDO(coupon.getId(), coupon.getShopNumber(), coupon.getName(), coupon.getSource(), coupon.getTarget(), coupon.getGoods(), coupon.getType(),
                    coupon.getValidStartTime(), coupon.getValidEndTime(), coupon.getStock(), coupon.getReceiveRule(), coupon.getConsumeRule(), coupon.getStatus(),
                    coupon.getCreateTime(), coupon.getUpdateTime(), coupon.getDelFlag(), discountRate);
        }
    }
}
