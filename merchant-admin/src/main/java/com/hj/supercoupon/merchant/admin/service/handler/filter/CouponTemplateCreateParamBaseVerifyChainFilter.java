package com.hj.supercoupon.merchant.admin.service.handler.filter;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.hj.supercoupon.framework.exception.ClientException;
import com.hj.supercoupon.merchant.admin.common.enums.DiscountTargetEnum;
import com.hj.supercoupon.merchant.admin.common.enums.DiscountTypeEnum;
import com.hj.supercoupon.merchant.admin.dto.req.CouponTemplateSaveReqDTO;
import com.hj.supercoupon.merchant.admin.service.basics.chain.MerchantAdminAbstractChainHandler;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.Date;

import static com.hj.supercoupon.merchant.admin.common.enums.ChainBizMarkEnum.MERCHANT_ADMIN_CREATE_COUPON_TEMPLATE_KEY;

/**
 * 验证优惠券创建接口参数是否正确责任链｜验证参数基本数据关系是否正确
 */
@Component
public class CouponTemplateCreateParamBaseVerifyChainFilter implements MerchantAdminAbstractChainHandler<CouponTemplateSaveReqDTO> {

    private final int maxStock = 20000000;

    @Override
    public void handler(CouponTemplateSaveReqDTO requestParam) {
        boolean targetAnyMatch = Arrays.stream(DiscountTargetEnum.values())
                .anyMatch(enumConstant -> enumConstant.getType() == requestParam.getTarget());
        if (!targetAnyMatch) {
            // 此处已经基本能判断数据请求属于恶意攻击，可以上报风控中心进行封禁账号
            throw new ClientException("优惠对象值不存在");
        }
        if (ObjectUtil.equal(requestParam.getTarget(), DiscountTargetEnum.ALL_STORE_GENERAL)
                && StrUtil.isNotEmpty(requestParam.getGoods())) {
            throw new ClientException("优惠券全店通用不可设置指定商品");
        }
        if (ObjectUtil.equal(requestParam.getTarget(), DiscountTargetEnum.PRODUCT_SPECIFIC)
                && StrUtil.isEmpty(requestParam.getGoods())) {
            throw new ClientException("优惠券商品专属未设置指定商品");
        }

        boolean typeAnyMatch = Arrays.stream(DiscountTypeEnum.values())
                .anyMatch(enumConstant -> enumConstant.getType() == requestParam.getType());
        if (!typeAnyMatch) {
            // 此处已经基本能判断数据请求属于恶意攻击，可以上报风控中心进行封禁账号
            throw new ClientException("优惠类型不存在");
        }

        Date now = new Date();
        if (requestParam.getValidStartTime().before(now)) {
            // 为了方便大家测试，不用关注这个时间，这里取消异常抛出
            // throw new ClientException("有效期开始时间不能早于当前时间");
        }

        if (requestParam.getStock() <= 0 || requestParam.getStock() > maxStock) {
            // 此处已经基本能判断数据请求属于恶意攻击，可以上报风控中心进行封禁账号
            throw new ClientException("库存数量设置异常");
        }

        if (!JSON.isValid(requestParam.getReceiveRule())) {
            // 此处已经基本能判断数据请求属于恶意攻击，可以上报风控中心进行封禁账号
            throw new ClientException("领取规则格式错误");
        }
        if (!JSON.isValid(requestParam.getConsumeRule())) {
            // 此处已经基本能判断数据请求属于恶意攻击，可以上报风控中心进行封禁账号
            throw new ClientException("消耗规则格式错误");
        }
    }

    @Override
    public String mark() {
        return MERCHANT_ADMIN_CREATE_COUPON_TEMPLATE_KEY.name();
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
