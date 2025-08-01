package com.hj.supercoupon.merchant.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hj.supercoupon.merchant.admin.dao.entity.CouponTemplateDO;
import com.hj.supercoupon.merchant.admin.dto.req.CouponTemplateSaveReqDTO;

/**
 * 优惠券模版业务逻辑层
 * @Author web-cat
 */
public interface CouponTemplateService extends IService<CouponTemplateDO> {
    /**
     * 创建商家优惠券模版
     * @param couponTemplateSaveReqDTO
     */
    void createCouponTemplate(CouponTemplateSaveReqDTO couponTemplateSaveReqDTO);
}
