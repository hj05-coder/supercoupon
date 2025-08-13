
package com.hj.supercoupon.distribution.mq.consumer;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hj.supercoupon.distribution.common.enums.CouponTaskStatusEnum;
import com.hj.supercoupon.distribution.common.enums.CouponTemplateStatusEnum;
import com.hj.supercoupon.distribution.dao.entity.CouponTaskDO;
import com.hj.supercoupon.distribution.dao.entity.CouponTemplateDO;
import com.hj.supercoupon.distribution.dao.mapper.CouponTaskMapper;
import com.hj.supercoupon.distribution.dao.mapper.CouponTemplateMapper;
import com.hj.supercoupon.distribution.dao.mapper.UserCouponMapper;
import com.hj.supercoupon.distribution.mq.base.MessageWrapper;
import com.hj.supercoupon.distribution.mq.event.CouponTaskExecuteEvent;
import com.hj.supercoupon.distribution.service.handler.excel.CouponTaskExcelObject;
import com.hj.supercoupon.distribution.service.handler.excel.ReadExcelDistributionListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 优惠券推送定时执行-真实执行消费者
 */
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = "super-coupon_distribution-service_coupon-task-execute_topic${unique-name:}",
        consumerGroup = "super-coupon_distribution-service_coupon-task-execute_cg${unique-name:}"
)
@Slf4j(topic = "CouponTaskExecuteConsumer")
public class CouponTaskExecuteConsumer implements RocketMQListener<MessageWrapper<CouponTaskExecuteEvent>> {

    private final CouponTaskMapper couponTaskMapper;
    private final CouponTemplateMapper couponTemplateMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final UserCouponMapper userCouponMapper;

    @Override
    public void onMessage(MessageWrapper<CouponTaskExecuteEvent> messageWrapper) {
        //开头打印日志,平常可Debug查看任务参数,线上可报平安(比如消息是否消费,重新投递时获取参数等)
        log.info("[消费者] 优惠券推送任务正式执行 - 执行消费逻辑，消息体：{}", JSON.toJSONString(messageWrapper));

        //判断优惠券模版模版发放状态是否为执行中,如果不是有可能是被取消状态
        Long couponTaskId = messageWrapper.getMessage().getCouponTaskId();
        CouponTaskDO couponTaskDO = couponTaskMapper.selectById(couponTaskId);
        if (ObjectUtil.notEqual(couponTaskDO.getStatus(), CouponTaskStatusEnum.IN_PROGRESS.getStatus())) {
            log.warn("[消费者] 优惠券推送任务正式执行 - 推送任务记录状态异常：{}，已终止推送", couponTaskDO.getStatus());
            return;
        }
        //判断优惠券是否正确
        LambdaQueryWrapper<CouponTemplateDO> queryWrapper = Wrappers.lambdaQuery(CouponTemplateDO.class)
                .eq(CouponTemplateDO::getId, couponTaskDO.getCouponTemplateId())
                .eq(CouponTemplateDO::getShopNumber, couponTaskDO.getShopNumber());
        CouponTemplateDO couponTemplateDO = couponTemplateMapper.selectOne(queryWrapper);
        Integer status = couponTemplateDO.getStatus();
        if (ObjectUtil.notEqual(status, CouponTemplateStatusEnum.ACTIVE.getStatus())) {
            log.error("[消费者] 优惠券推送任务正式执行 - 优惠券ID：{}，优惠券模板状态：{}", couponTaskDO.getCouponTemplateId(), status);
            return;
        }

        //正式开始执行优惠券推送任务
        ReadExcelDistributionListener readExcelDistributionListener = new ReadExcelDistributionListener(
                couponTaskId,
                couponTemplateDO,
                stringRedisTemplate,
                couponTemplateMapper,
                userCouponMapper,
                couponTaskMapper
        );
        EasyExcel.read(couponTaskDO.getFileAddress(), CouponTaskExcelObject.class, readExcelDistributionListener).sheet().doRead();
    }
}
