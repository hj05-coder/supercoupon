package com.hj.supercoupon.engine.dao.entity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户预约提醒信息存储数据库持久层实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("t_coupon_template_remind")
public class CouponTemplateRemindDO {

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 券id
     */
    private Long couponTemplateId;

    /**
     * 用户预约信息，用位图存储信息
     */
    private Long information;

    /**
     * 店铺编号
     */
    private Long shopNumber;

    /**
     * 优惠券开抢时间
     */
    private Date startTime;
}
