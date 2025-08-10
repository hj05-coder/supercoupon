package com.hj.supercoupon.engine.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hj.supercoupon.engine.dao.entity.CouponTemplateDO;
import com.hj.supercoupon.engine.dto.req.CouponTemplateQueryReqDTO;
import com.hj.supercoupon.engine.dto.resp.CouponTemplateQueryRespDTO;


/**
 * 优惠券模板业务逻辑层
 */
public interface CouponTemplateService extends IService<CouponTemplateDO> {

    /**
     * 查询优惠券模板
     *
     * @param requestParam 请求参数
     * @return 优惠券模板信息
     */
    CouponTemplateQueryRespDTO findCouponTemplate(CouponTemplateQueryReqDTO requestParam);
}
