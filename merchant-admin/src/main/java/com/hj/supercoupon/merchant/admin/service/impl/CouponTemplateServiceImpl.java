package com.hj.supercoupon.merchant.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hj.supercoupon.merchant.admin.common.constant.MerchantAdminRedisConstant;
import com.hj.supercoupon.merchant.admin.common.context.UserContext;
import com.hj.supercoupon.merchant.admin.common.enums.CouponTemplateStatusEnum;
import com.hj.supercoupon.merchant.admin.dao.entity.CouponTemplateDO;
import com.hj.supercoupon.merchant.admin.dao.mapper.CouponTemplateMapper;
import com.hj.supercoupon.merchant.admin.dto.req.CouponTemplateSaveReqDTO;
import com.hj.supercoupon.merchant.admin.dto.resp.CouponTemplateQueryRespDTO;
import com.hj.supercoupon.merchant.admin.service.CouponTemplateService;
import com.hj.supercoupon.merchant.admin.service.basics.chain.MerchantAdminChainContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.hj.supercoupon.merchant.admin.common.enums.ChainBizMarkEnum.MERCHANT_ADMIN_CREATE_COUPON_TEMPLATE_KEY;

/**
 * 优惠券模版业务逻辑实现层
 * @Author web-cat
 */
@Service
@RequiredArgsConstructor
public class CouponTemplateServiceImpl extends ServiceImpl<CouponTemplateMapper, CouponTemplateDO> implements CouponTemplateService {
    private final CouponTemplateMapper couponTemplateMapper;
    private final MerchantAdminChainContext merchantAdminChainContext;
    private final StringRedisTemplate stringRedisTemplate;
    @Override
    public void createCouponTemplate(CouponTemplateSaveReqDTO couponTemplateSaveReqDTO) {
        //通过责任链验证请求参数是否正确
        merchantAdminChainContext.handler(MERCHANT_ADMIN_CREATE_COUPON_TEMPLATE_KEY.name(), couponTemplateSaveReqDTO);
        //新增优惠券模版信息到数据库
        CouponTemplateDO couponTemplateDO = BeanUtil.toBean(couponTemplateSaveReqDTO, CouponTemplateDO.class);
        couponTemplateDO.setStatus(CouponTemplateStatusEnum.ACTIVE.getStatus());
        couponTemplateDO.setShopNumber(UserContext.getShopNumber());
        couponTemplateMapper.insert(couponTemplateDO);

        // 缓存预热：通过将数据库的记录序列化成 JSON 字符串放入 Redis 缓存
        CouponTemplateQueryRespDTO actualRespDTO = BeanUtil.toBean(couponTemplateDO, CouponTemplateQueryRespDTO.class);
        Map<String, Object> cacheTargetMap = BeanUtil.beanToMap(actualRespDTO, false, true);
        Map<String, String> actualCacheTargetMap = cacheTargetMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue() != null ? entry.getValue().toString() : ""
                ));
        String couponTemplateCacheKey = String.format(MerchantAdminRedisConstant.COUPON_TEMPLATE_KEY, couponTemplateDO.getId());

        // 通过 LUA 脚本执行设置 Hash 数据以及设置过期时间
        String luaScript = "redis.call('HMSET', KEYS[1], unpack(ARGV, 1, #ARGV - 1)) " +
                "redis.call('EXPIREAT', KEYS[1], ARGV[#ARGV])";

        List<String> keys = Collections.singletonList(couponTemplateCacheKey);
        List<String> args = new ArrayList<>(actualCacheTargetMap.size() * 2 + 1);
        actualCacheTargetMap.forEach((key, value) -> {
            args.add(key);
            args.add(value);
        });
        // 优惠券活动过期时间转换为秒级别的 Unix 时间戳
        args.add(String.valueOf(couponTemplateDO.getValidEndTime().getTime() / 1000));

        // 执行 LUA 脚本
        stringRedisTemplate.execute(
                new DefaultRedisScript<>(luaScript, Long.class),
                keys,
                args.toArray()
        );
    }
}
