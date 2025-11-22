package com.hj.supercoupon.settlement.dao.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 立减券（无门槛）数据库持久层实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FixedDiscountCouponDO extends CouponTemplateDO {

    /**
     * 优惠金额
     */
    private Integer discountAmount;

    @Builder(builderMethodName = "fixedDiscountCouponBuilder")
    public FixedDiscountCouponDO(CouponTemplateDO coupon, Integer discountAmount) {
        super(coupon.getId(), coupon.getShopNumber(), coupon.getName(), coupon.getSource(), coupon.getTarget(), coupon.getGoods(), coupon.getType(),
                coupon.getValidStartTime(), coupon.getValidEndTime(), coupon.getStock(), coupon.getReceiveRule(), coupon.getConsumeRule(), coupon.getStatus(),
                coupon.getCreateTime(), coupon.getUpdateTime(), coupon.getDelFlag());
        setDiscountAmount(discountAmount);
    }

    public static FixedDiscountCouponDOBuilder builder() {
        return new FixedDiscountCouponDOBuilder();
    }

    public static class FixedDiscountCouponDOBuilder extends CouponTemplateDO.CouponTemplateDOBuilder {
        private Integer discountAmount;

        FixedDiscountCouponDOBuilder() {
            super();
        }

        public FixedDiscountCouponDOBuilder discountAmount(Integer discountAmount) {
            this.discountAmount = discountAmount;
            return this;
        }

        @Override
        public FixedDiscountCouponDO build() {
            CouponTemplateDO coupon = super.build();
            return new FixedDiscountCouponDO(coupon, discountAmount);
        }
    }
}