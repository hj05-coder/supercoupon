package com.hj.supercoupon.merchant.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hj.supercoupon.merchant.admin.dao.entity.CouponTemplateDO;
import com.hj.supercoupon.merchant.admin.dto.req.CouponTemplateNumberReqDTO;
import com.hj.supercoupon.merchant.admin.dto.req.CouponTemplatePageQueryReqDTO;
import com.hj.supercoupon.merchant.admin.dto.req.CouponTemplateSaveReqDTO;
import com.hj.supercoupon.merchant.admin.dto.resp.CouponTemplatePageQueryRespDTO;
import com.hj.supercoupon.merchant.admin.dto.resp.CouponTemplateQueryRespDTO;

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

    /**
     * 分页查询商家优惠券模版
     * @param requestParam 请求参数
     * @return 商家优惠券模版分页数据
     */
    IPage<CouponTemplatePageQueryRespDTO> pageQueryCouponTemplate(CouponTemplatePageQueryReqDTO requestParam);

    /**
     * 查询优惠券模版详情
     * 后管接口并不存在并发, 直接查询数据库即可
     * @param couponTemplateId 优惠券模版ID
     * @return 优惠券模版详情
     */
    CouponTemplateQueryRespDTO findCouponTemplateById(String couponTemplateId);

    /**
     * 增加优惠券模版发行量
     * @param requestParam
     */
    void increaseNumberCouponTemplate(CouponTemplateNumberReqDTO requestParam);

    /**
     * 结束优惠券模版
     * @param couponTemplateId 优惠券末班那ID
     */
    void terminateCouponTemplate(String couponTemplateId);
}
