package com.hj.supercoupon.engine.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hj.supercoupon.engine.common.context.UserContext;
import com.hj.supercoupon.engine.dao.entity.CouponTemplateRemindDO;
import com.hj.supercoupon.engine.dao.mapper.CouponTemplateRemindMapper;
import com.hj.supercoupon.engine.dto.req.CouponTemplateQueryReqDTO;
import com.hj.supercoupon.engine.dto.req.CouponTemplateRemindCancelReqDTO;
import com.hj.supercoupon.engine.dto.req.CouponTemplateRemindCreateReqDTO;
import com.hj.supercoupon.engine.dto.req.CouponTemplateRemindQueryReqDTO;
import com.hj.supercoupon.engine.dto.resp.CouponTemplateQueryRespDTO;
import com.hj.supercoupon.engine.dto.resp.CouponTemplateRemindQueryRespDTO;
import com.hj.supercoupon.engine.mq.event.CouponTemplateRemindDelayEvent;
import com.hj.supercoupon.engine.mq.producer.CouponTemplateRemindDelayProducer;
import com.hj.supercoupon.engine.service.CouponTemplateRemindService;
import com.hj.supercoupon.engine.service.CouponTemplateService;
import com.hj.supercoupon.engine.service.handler.remind.dto.CouponTemplateRemindDTO;
import com.hj.supercoupon.engine.toolkit.CouponTemplateRemindUtil;
import com.hj.supercoupon.framework.exception.ClientException;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

import static com.hj.supercoupon.engine.common.constant.EngineRedisConstant.USER_COUPON_TEMPLATE_REMIND_INFORMATION;

/**
 * 优惠券预约提醒业务逻辑实现层
 * @author web-cat
 */
@Service
@RequiredArgsConstructor
public class CouponTemplateRemindServiceImpl extends ServiceImpl<CouponTemplateRemindMapper, CouponTemplateRemindDO> implements CouponTemplateRemindService {

    private final CouponTemplateRemindMapper couponTemplateRemindMapper;
    private final CouponTemplateService couponTemplateService;
    private final RBloomFilter<String> cancelRemindBloomFilter;
    private final CouponTemplateRemindDelayProducer couponRemindDelayProducer;
    private final StringRedisTemplate stringRedisTemplate;
    @Override
    public void createCouponRemind(CouponTemplateRemindCreateReqDTO requestParam) {
        //验证优惠券是否存在,避免缓存穿透问题并获取优惠券开抢时间
        CouponTemplateQueryRespDTO couponTemplate = couponTemplateService.
                findCouponTemplate(new CouponTemplateQueryReqDTO(requestParam.getShopNumber(), requestParam.getCouponTemplateId()));
        if (couponTemplate.getValidStartTime().before(new Date())){
            throw new ClientException("无法预约已开始领取的优惠券");
        }
        //查询用户是否已经预约过优惠券的提醒信息
        LambdaQueryWrapper<CouponTemplateRemindDO> queryWrapper = Wrappers.lambdaQuery(CouponTemplateRemindDO.class)
                .eq(CouponTemplateRemindDO::getUserId, UserContext.getUserId())
                .eq(CouponTemplateRemindDO::getCouponTemplateId, requestParam.getCouponTemplateId());
        CouponTemplateRemindDO couponTemplateRemindDO = couponTemplateRemindMapper.selectOne(queryWrapper);
        //如果没创建过期提醒
        if (couponTemplateRemindDO == null){
            couponTemplateRemindDO = BeanUtil.toBean(requestParam, CouponTemplateRemindDO.class);

            //设置优惠券开抢时间
            couponTemplateRemindDO.setStartTime(couponTemplate.getValidStartTime());
            couponTemplateRemindDO.setInformation(CouponTemplateRemindUtil.calculateBitMap(requestParam.getRemindTime(), requestParam.getType()));
            couponTemplateRemindDO.setUserId(Long.parseLong(UserContext.getUserId()));

            couponTemplateRemindMapper.insert(couponTemplateRemindDO);
        }else {
            Long information = couponTemplateRemindDO.getInformation();
            Long bitMap = CouponTemplateRemindUtil.calculateBitMap(requestParam.getRemindTime(), requestParam.getType());
            if ((information & bitMap) != 0L) {
                throw new ClientException("已经创建过该提醒了");
            }
            couponTemplateRemindDO.setInformation(information ^ bitMap);
            couponTemplateRemindMapper.update(couponTemplateRemindDO, queryWrapper);
        }

        //发送预约提醒抢购优惠券延时消息
        CouponTemplateRemindDelayEvent couponTemplateRemindDelayEvent = CouponTemplateRemindDelayEvent.builder()
                .couponTemplateId(couponTemplate.getId())
                .userId(UserContext.getUserId())
                .contact(UserContext.getUserId())
                .shopNumber(couponTemplate.getShopNumber())
                .type(requestParam.getType())
                .remindTime(requestParam.getRemindTime())
                .startTime(couponTemplate.getValidStartTime())
                .delayTime(DateUtil.offsetMinute(couponTemplate.getValidStartTime(), -requestParam.getRemindTime()).getTime())
                .build();
        couponRemindDelayProducer.sendMessage(couponTemplateRemindDelayEvent);

        //删除用户预约提醒的缓存信息,通过更新数据库删除缓存策略保障数据库和缓存一致性
        stringRedisTemplate.delete(String.format(USER_COUPON_TEMPLATE_REMIND_INFORMATION, UserContext.getUserId()));
    }

    @Override
    public List<CouponTemplateRemindQueryRespDTO> listCouponRemind(CouponTemplateRemindQueryReqDTO requestParam) {
        return List.of();
    }

    @Override
    public void cancelCouponRemind(CouponTemplateRemindCancelReqDTO requestParam) {

    }

    @Override
    public boolean isCancelRemind(CouponTemplateRemindDTO requestParam) {
        return false;
    }
}
