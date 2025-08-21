package com.hj.supercoupon.engine.service.handler.remind.impl;

import com.hj.supercoupon.engine.service.handler.remind.RemindCouponTemplate;
import com.hj.supercoupon.engine.service.handler.remind.dto.CouponTemplateRemindDTO;
import org.springframework.stereotype.Component;

/**
 * 应用 App 弹框方式提醒用户抢券
 */
@Component
public class SendAppMessageRemindCouponTemplate implements RemindCouponTemplate {

    /**
     * 应用 App 弹框方式提醒用户抢券
     *
     * @param couponTemplateRemindDTO 提醒所需要的信息
     */
    @Override
    public boolean remind(CouponTemplateRemindDTO couponTemplateRemindDTO) {
        // 空实现
        return true;
    }
}
