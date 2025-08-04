package com.hj.supercoupon.merchant.admin.service;

import com.hj.supercoupon.merchant.admin.dto.req.CouponTaskCreateReqDTO;

/**
 * 优惠券推送业务逻辑层
 * @author web-cat
 */
public interface CouponTaskService {
    /**
     * 商家创建优惠券推送任务
     * @param requestParam
     */
    void createCouponTask(CouponTaskCreateReqDTO requestParam);
}
