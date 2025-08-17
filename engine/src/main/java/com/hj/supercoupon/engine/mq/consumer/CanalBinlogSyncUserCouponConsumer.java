
package com.hj.supercoupon.engine.mq.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.hj.supercoupon.engine.common.constant.EngineRedisConstant;
import com.hj.supercoupon.engine.mq.event.CanalBinlogEvent;
import com.hj.supercoupon.engine.mq.event.UserCouponDelayCloseEvent;
import com.hj.supercoupon.engine.mq.producer.UserCouponDelayCloseProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * 通过 Canal 监听用户优惠券表 Binlog 投递消息队列消费
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = "super-coupon_canal_engine-service_common-sync_topic${unique-name:}",
        consumerGroup = "super-coupon_canal_engine-service_common-sync_cg${unique-name:}"
)
public class CanalBinlogSyncUserCouponConsumer implements RocketMQListener<CanalBinlogEvent> {

    private final StringRedisTemplate stringRedisTemplate;
    private final UserCouponDelayCloseProducer couponDelayCloseProducer;

    @Value("${super-coupon.user-coupon-list.save-cache.type:direct}")
    private String userCouponListSaveCacheType;

    @Override
    public void onMessage(CanalBinlogEvent canalBinlogEvent) {
        if (ObjectUtil.notEqual(userCouponListSaveCacheType, "binlog")) {
            return;
        }
        Map<String, Object> first = CollUtil.getFirst(canalBinlogEvent.getData());
        String couponTemplateId = first.get("coupon_template_id").toString();
        String userCouponId = first.get("id").toString();
        // 用户优惠券创建事件
        if (ObjectUtil.equal(canalBinlogEvent.getType(), "INSERT")) {
            // 添加用户领取优惠券模板缓存记录
            String userCouponListCacheKey = String.format(EngineRedisConstant.USER_COUPON_TEMPLATE_LIST_KEY, first.get("user_id").toString());
            String userCouponItemCacheKey = StrUtil.builder()
                    .append(couponTemplateId)
                    .append("_")
                    .append(userCouponId)
                    .toString();
            Date receiveTime = DateUtil.parse(first.get("receive_time").toString());
            stringRedisTemplate.opsForZSet().add(userCouponListCacheKey, userCouponItemCacheKey, receiveTime.getTime());

            // 由于 Redis 在持久化或主从复制的极端情况下可能会出现数据丢失，而我们对指令丢失几乎无法容忍，因此我们采用经典的写后查询策略来应对这一问题
            Double scored;
            try {
                scored = stringRedisTemplate.opsForZSet().score(userCouponListCacheKey, userCouponItemCacheKey);
                // scored 为空意味着可能 Redis Cluster 主从同步丢失了数据，比如 Redis 主节点还没有同步到从节点就宕机了，解决方案就是再新增一次
                if (scored == null) {
                    // 如果这里也新增失败了怎么办？我们大概率做不到绝对的万无一失，只能尽可能增加成功率
                    stringRedisTemplate.opsForZSet().add(userCouponListCacheKey, userCouponItemCacheKey, receiveTime.getTime());
                }
            } catch (Throwable ex) {
                log.warn("查询Redis用户优惠券记录为空或抛异常，可能Redis宕机或主从复制数据丢失，基础错误信息：{}", ex.getMessage());
                // 如果直接抛异常大概率 Redis 宕机了，所以应该写个延时队列向 Redis 重试放入值。为了避免代码复杂性，这里直接写新增，大家知道最优解决方案即可
                stringRedisTemplate.opsForZSet().add(userCouponListCacheKey, userCouponItemCacheKey, receiveTime.getTime());
            }

            // 发送延时消息队列，等待优惠券到期后，将优惠券信息从缓存中删除
            UserCouponDelayCloseEvent userCouponDelayCloseEvent = UserCouponDelayCloseEvent.builder()
                    .couponTemplateId(couponTemplateId)
                    .userCouponId(userCouponId)
                    .userId(first.get("user_id").toString())
                    .delayTime(DateUtil.parse(first.get("valid_end_time").toString()).getTime())
                    .build();
            SendResult sendResult = couponDelayCloseProducer.sendMessage(userCouponDelayCloseEvent);

            // 发送消息失败解决方案简单且高效的逻辑之一：打印日志并报警，通过日志搜集并重新投递
            if (ObjectUtil.notEqual(sendResult.getSendStatus().name(), "SEND_OK")) {
                log.warn("发送优惠券关闭延时队列失败，消息参数：{}", JSON.toJSONString(userCouponDelayCloseEvent));
            }
        }
    }
}
