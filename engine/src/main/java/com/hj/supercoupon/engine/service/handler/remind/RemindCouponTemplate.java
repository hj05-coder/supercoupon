package com.hj.supercoupon.engine.service.handler.remind;
import com.hj.supercoupon.engine.service.handler.remind.dto.CouponTemplateRemindDTO;

/**
 * 优惠券抢券提醒接口
 */
public interface RemindCouponTemplate {

    /**
     * 提醒用户抢券
     *
     * @param remindCouponTemplateDTO 提醒所需要的信息
     */
    boolean remind(CouponTemplateRemindDTO remindCouponTemplateDTO);
}
