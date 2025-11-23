package com.hj.supercoupon.engine.mq.consumer;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.hj.supercoupon.engine.common.constant.EngineRedisConstant;
import com.hj.supercoupon.engine.common.enums.UserCouponStatusEnum;
import com.hj.supercoupon.engine.dao.entity.UserCouponDO;
import com.hj.supercoupon.engine.dao.mapper.CouponTemplateMapper;
import com.hj.supercoupon.engine.dao.mapper.UserCouponMapper;
import com.hj.supercoupon.engine.dto.req.CouponTemplateRedeemReqDTO;
import com.hj.supercoupon.engine.dto.resp.CouponTemplateQueryRespDTO;
import com.hj.supercoupon.engine.mq.base.MessageWrapper;
import com.hj.supercoupon.engine.mq.event.UserCouponDelayCloseEvent;
import com.hj.supercoupon.engine.mq.event.UserCouponRedeemEvent;
import com.hj.supercoupon.engine.mq.producer.UserCouponDelayCloseProducer;
import com.hj.supercoupon.framework.idempotent.NoMQDuplicateConsume;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 用户兑换优惠券消息消费者
 */
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = "super-coupon_engine-service_coupon-redeem_topic${unique-name:}",
        consumerGroup = "super-coupon_engine-service_coupon-redeem_cg${unique-name:}"
)
@Slf4j(topic = "UserCouponRedeemConsumer")
public class UserCouponRedeemConsumer implements RocketMQListener<MessageWrapper<UserCouponRedeemEvent>> {

    private final UserCouponMapper userCouponMapper;
    private final CouponTemplateMapper couponTemplateMapper;
    private final UserCouponDelayCloseProducer couponDelayCloseProducer;
    private final StringRedisTemplate stringRedisTemplate;

    @NoMQDuplicateConsume(
            keyPrefix = "user-coupon-redeem:",
            key = "#messageWrapper.keys",
            keyTimeout = 600
    )      //进行压测，需要将这个幂等注释掉
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void onMessage(MessageWrapper<UserCouponRedeemEvent> messageWrapper) {
        // 开头打印日志，平常可 Debug 看任务参数，线上可报平安（比如消息是否消费，重新投递时获取参数等）
        log.info("[消费者] 用户兑换优惠券 - 执行消费逻辑，消息体：{}", JSON.toJSONString(messageWrapper));

        CouponTemplateRedeemReqDTO requestParam = messageWrapper.getMessage().getRequestParam();
        CouponTemplateQueryRespDTO couponTemplate = messageWrapper.getMessage().getCouponTemplate();
        String userId = messageWrapper.getMessage().getUserId();

        int decremented = couponTemplateMapper.decrementCouponTemplateStock(Long.parseLong(requestParam.getShopNumber()), Long.parseLong(requestParam.getCouponTemplateId()), 1L);
        if (!SqlHelper.retBool(decremented)) {
            log.warn("[消费者] 用户兑换优惠券 - 执行消费逻辑，扣减优惠券数据库库存失败，消息体：{}", JSON.toJSONString(messageWrapper));
            return;
        }

        // 添加 Redis 用户领取的优惠券记录列表
        Date now = new Date();
        DateTime validEndTime = DateUtil.offsetHour(now, JSON.parseObject(couponTemplate.getConsumeRule()).getInteger("validityPeriod"));
        UserCouponDO userCouponDO = UserCouponDO.builder()
                .couponTemplateId(Long.parseLong(requestParam.getCouponTemplateId()))
                .userId(Long.parseLong(userId))
                .source(requestParam.getSource())
                .receiveCount(messageWrapper.getMessage().getReceiveCount())
                .status(UserCouponStatusEnum.UNUSED.getCode())
                .receiveTime(now)
                .validStartTime(now)
                .validEndTime(validEndTime)
                .build();
        userCouponMapper.insert(userCouponDO);

        // 添加用户领取优惠券模板缓存记录
        String userCouponListCacheKey = String.format(EngineRedisConstant.USER_COUPON_TEMPLATE_LIST_KEY, userId);
        String userCouponItemCacheKey = StrUtil.builder()
                .append(requestParam.getCouponTemplateId())
                .append("_")
                .append(userCouponDO.getId())
                .toString();
        stringRedisTemplate.opsForZSet().add(userCouponListCacheKey, userCouponItemCacheKey, now.getTime());

        // 由于 Redis 在持久化或主从复制的极端情况下可能会出现数据丢失，而我们对指令丢失几乎无法容忍，因此我们采用经典的写后查询策略来应对这一问题
        Double scored;
        try {
            scored = stringRedisTemplate.opsForZSet().score(userCouponListCacheKey, userCouponItemCacheKey);
            // scored 为空意味着可能 Redis Cluster 主从同步丢失了数据，比如 Redis 主节点还没有同步到从节点就宕机了，解决方案就是再新增一次
            if (scored == null) {
                // 如果这里也新增失败了怎么办？我们大概率做不到绝对的万无一失，只能尽可能增加成功率
                stringRedisTemplate.opsForZSet().add(userCouponListCacheKey, userCouponItemCacheKey, now.getTime());
            }
        } catch (Throwable ex) {
            log.warn("[消费者] 用户兑换优惠券 - 执行消费逻辑，查询Redis用户优惠券记录为空或抛异常，可能Redis宕机或主从复制数据丢失，基础错误信息：{}", ex.getMessage());
            // 如果直接抛异常大概率 Redis 宕机了，所以应该写个延时队列向 Redis 重试放入值。为了避免代码复杂性，这里直接写新增，大家知道最优解决方案即可
            stringRedisTemplate.opsForZSet().add(userCouponListCacheKey, userCouponItemCacheKey, now.getTime());
        }

        // 发送延时消息队列，等待优惠券到期后，将优惠券信息从缓存中删除
        UserCouponDelayCloseEvent userCouponDelayCloseEvent = UserCouponDelayCloseEvent.builder()
                .couponTemplateId(requestParam.getCouponTemplateId())
                .userCouponId(String.valueOf(userCouponDO.getId()))
                .userId(userId)
                .delayTime(validEndTime.getTime())
                .build();
        SendResult sendResult = couponDelayCloseProducer.sendMessage(userCouponDelayCloseEvent);

        // 发送消息失败解决方案简单且高效的逻辑之一：打印日志并报警，通过日志搜集并重新投递
        if (ObjectUtil.notEqual(sendResult.getSendStatus().name(), "SEND_OK")) {
            log.warn("[消费者] 用户兑换优惠券 - 执行消费逻辑，发送优惠券关闭延时队列失败，消息参数：{}", JSON.toJSONString(userCouponDelayCloseEvent));
        }
    }
}
