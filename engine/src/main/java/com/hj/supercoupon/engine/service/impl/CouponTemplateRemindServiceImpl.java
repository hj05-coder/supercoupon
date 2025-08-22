package com.hj.supercoupon.engine.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hj.supercoupon.engine.common.context.UserContext;
import com.hj.supercoupon.engine.dao.entity.CouponTemplateDO;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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
        String value = stringRedisTemplate.opsForValue().get(String.format(USER_COUPON_TEMPLATE_REMIND_INFORMATION, requestParam.getUserId()));
        if (value != null) {
            return JSON.parseArray(value, CouponTemplateRemindQueryRespDTO.class);
        }
        //查询出用户预约的信息
        LambdaQueryWrapper<CouponTemplateRemindDO> queryWrapper = Wrappers.lambdaQuery(CouponTemplateRemindDO.class)
                .eq(CouponTemplateRemindDO::getUserId, requestParam.getUserId());
        List<CouponTemplateRemindDO> couponTemplateRemindDOlist = couponTemplateRemindMapper.selectList(queryWrapper);
        if (CollUtil.isEmpty(couponTemplateRemindDOlist))
            return new ArrayList<>();
        //根据优惠券id查询优惠券信息
        List<Long> couponIds = couponTemplateRemindDOlist.stream()
                .map(CouponTemplateRemindDO::getCouponTemplateId)
                .toList();
        List<Long> shopNumbers = couponTemplateRemindDOlist.stream()
                .map(CouponTemplateRemindDO::getShopNumber)
                .toList();
        List<CouponTemplateDO> couponTemplateDOList = couponTemplateService.listCouponTemplateByIds(couponIds, shopNumbers);
        List<CouponTemplateRemindQueryRespDTO> actualResult = BeanUtil.copyToList(couponTemplateDOList, CouponTemplateRemindQueryRespDTO.class);

        // 填充响应结果的其它信息
        actualResult.forEach(each -> {
            // 找到当前优惠券对应的预约提醒信息
            couponTemplateRemindDOlist.stream()
                    .filter(i -> i.getCouponTemplateId().equals(each.getId()))
                    .findFirst()
                    .ifPresent(i -> {
                        // 解析并填充预约提醒信息
                        CouponTemplateRemindUtil.fillRemindInformation(each, i.getInformation());
                    });
        });

        stringRedisTemplate.opsForValue().set(String.format(USER_COUPON_TEMPLATE_REMIND_INFORMATION, requestParam.getUserId()), JSON.toJSONString(actualResult), 1, TimeUnit.MINUTES);
        return actualResult;
    }

    @Override
    @Transactional
    public void cancelCouponRemind(CouponTemplateRemindCancelReqDTO requestParam) {
        //验证优惠券是否存在,避免缓存穿透问题并获取优惠券开抢时间
        CouponTemplateQueryRespDTO couponTemplate = couponTemplateService
                .findCouponTemplate(new CouponTemplateQueryReqDTO(requestParam.getShopNumber(), requestParam.getCouponTemplateId()));
        if (couponTemplate.getValidStartTime().before(new Date())){
            throw new ClientException("无法取消已开始领取的优惠券预约");
        }
        LambdaQueryWrapper<CouponTemplateRemindDO> queryWrapper = Wrappers.lambdaQuery(CouponTemplateRemindDO.class)
                .eq(CouponTemplateRemindDO::getUserId, UserContext.getUserId())
                .eq(CouponTemplateRemindDO::getCouponTemplateId, requestParam.getCouponTemplateId());
        CouponTemplateRemindDO couponTemplateRemindDO = couponTemplateRemindMapper.selectOne(queryWrapper);
        if (couponTemplateRemindDO == null){
            throw new ClientException("优惠券模版预约信息不存在");
        }
        //计算bitmap信息
        Long bitMap = CouponTemplateRemindUtil.calculateBitMap(requestParam.getRemindTime(), requestParam.getType());
        if ((bitMap & couponTemplateRemindDO.getInformation()) == 0L) {
            throw new ClientException("您没有预约该时间点的提醒");
        }

        bitMap ^= couponTemplateRemindDO.getInformation();
        queryWrapper.eq(CouponTemplateRemindDO::getInformation, couponTemplateRemindDO.getInformation());
        if (bitMap.equals(0L)) {
            // 如果新 BitMap 信息是 0，说明已经没有预约提醒了，可以直接删除
            if (couponTemplateRemindMapper.delete(queryWrapper) == 0) {
                // MySQL 乐观锁进行删除，如果删除失败，说明用户可能同时正在进行删除、新增提醒操作
                throw new ClientException("取消提醒失败，请刷新页面后重试");
            }
        } else {
            // 虽然删除了这个预约提醒，但还有其它提醒，那就更新数据库
            couponTemplateRemindDO.setInformation(bitMap);
            if (couponTemplateRemindMapper.update(couponTemplateRemindDO, queryWrapper) == 0) {
                // MySQL 乐观锁进行更新，如果更新失败，说明用户可能同时正在进行删除、新增提醒操作
                throw new ClientException("取消提醒失败，请刷新页面后重试");
            }
        }

        // 取消提醒这个信息添加到布隆过滤器中
        cancelRemindBloomFilter.add(String.valueOf
                (Objects.hash(requestParam.getCouponTemplateId(), UserContext.getUserId(), requestParam.getRemindTime(), requestParam.getType())));
        // 删除用户预约提醒的缓存信息，通过更新数据库删除缓存策略保障数据库和缓存一致性
        stringRedisTemplate.delete(String.format(USER_COUPON_TEMPLATE_REMIND_INFORMATION, UserContext.getUserId()));
    }

    @Override
    public boolean isCancelRemind(CouponTemplateRemindDTO requestParam) {
        if (!cancelRemindBloomFilter.contains(String.valueOf(Objects.hash(requestParam.getCouponTemplateId(), requestParam.getUserId(), requestParam.getRemindTime(), requestParam.getType())))) {
            // 布隆过滤器中不存在，说明没取消提醒，此时已经能挡下大部分请求
            return false;
        }
        // 对于少部分的“取消了预约”，可能是误判，此时需要去数据库中查找
        LambdaQueryWrapper<CouponTemplateRemindDO> queryWrapper = Wrappers.lambdaQuery(CouponTemplateRemindDO.class)
                .eq(CouponTemplateRemindDO::getUserId, requestParam.getUserId())
                .eq(CouponTemplateRemindDO::getCouponTemplateId, requestParam.getCouponTemplateId());
        CouponTemplateRemindDO couponTemplateRemindDO = couponTemplateRemindMapper.selectOne(queryWrapper);
        if (couponTemplateRemindDO == null){
            //数据库中没该条预约提醒,说明被取消
            return true;
        }
        // 即使存在数据，也要检查该类型的该时间点是否有提醒
        Long information = couponTemplateRemindDO.getInformation();
        Long bitMap = CouponTemplateRemindUtil.calculateBitMap(requestParam.getRemindTime(), requestParam.getType());

        // 按位与等于 0 说明用户取消了预约
        return (bitMap & information) == 0L;
    }
}
