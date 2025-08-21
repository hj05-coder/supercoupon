package com.hj.supercoupon.engine.service.handler.remind.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 发送抢券提醒实体
 */
@Data
@Schema(description = "发送抢券提醒实体")
public class CouponTemplateRemindDTO {

    /**
     * 优惠券模板id
     */
    @Schema(description = " ", example = "1810966706881941507", required = true)
    private String couponTemplateId;

    /**
     * 店铺编号
     */
    @Schema(description = "店铺编号", example = "1810714735922956666", required = true)
    private String shopNumber;

    /**
     * 用户id
     */
    @Schema(description = "用户id", example = "1810868149847928832", required = true)
    private String userId;

    /**
     * 用户联系方式，可能是邮箱、手机号、等等
     */
    @Schema(description = "用户联系方式")
    private String contact;

    /**
     * 提醒方式
     */
    @Schema(description = "提醒方式", example = "0", required = true)
    private Integer type;

    /**
     * 提醒时间，比如五分钟，十分钟，十五分钟
     */
    @Schema(description = "提醒时间")
    private Integer remindTime;

    /**
     * 开抢时间
     */
    @Schema(description = "开抢时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;
}
