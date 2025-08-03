package com.hj.supercoupon.merchant.admin.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hj.supercoupon.merchant.admin.dao.entity.CouponTemplateDO;
import org.apache.ibatis.annotations.Param;


/**
 * 优惠券模板数据库持久层
 */
public interface CouponTemplateMapper extends BaseMapper<CouponTemplateDO> {
    /**
     * 增加优惠券模版发行量
     * @param shopNumber  店铺编号
     * @param couponTemplateId 优惠券模版ID
     * @param number 增加发行数量
     */
    int increaseNumberCouponTemplate(@Param("shopNumber") Long shopNumber, @Param("couponTemplateId") String couponTemplateId, @Param("number") Integer number);
}
