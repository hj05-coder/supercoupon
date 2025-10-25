package com.hj.supercoupon.merchant.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hj.supercoupon.framework.exception.ClientException;
import com.hj.supercoupon.merchant.admin.common.context.UserContext;
import com.hj.supercoupon.merchant.admin.common.enums.CouponTaskSendTypeEnum;
import com.hj.supercoupon.merchant.admin.common.enums.CouponTaskStatusEnum;
import com.hj.supercoupon.merchant.admin.dao.entity.CouponTaskDO;
import com.hj.supercoupon.merchant.admin.dao.mapper.CouponTaskMapper;
import com.hj.supercoupon.merchant.admin.dto.req.CouponTaskCreateReqDTO;
import com.hj.supercoupon.merchant.admin.dto.resp.CouponTemplateQueryRespDTO;
import com.hj.supercoupon.merchant.admin.service.CouponTaskService;
import com.hj.supercoupon.merchant.admin.service.CouponTemplateService;
import com.hj.supercoupon.merchant.admin.service.handler.excel.RowCountListener;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 优惠券推送业务逻辑实现蹭
 * @author web-cat
 */
@Service
@RequiredArgsConstructor
public class CouponTaskServiceImpl extends ServiceImpl<CouponTaskMapper, CouponTaskDO> implements CouponTaskService {

    private final CouponTemplateService couponTemplateService;
    private final CouponTaskMapper couponTaskMapper;

    @Override
    public void createCouponTask(CouponTaskCreateReqDTO requestParam) {
        CouponTemplateQueryRespDTO template = couponTemplateService.findCouponTemplateById(requestParam.getCouponTemplateId());
        if (template == null) {
            throw new ClientException("优惠券模版不存在,请检查提交信息是否正确");
        }
        //参数验证
        validateRequestParam(requestParam);
        // 构建优惠券推送任务数据库持久层实体
        CouponTaskDO couponTaskDO = BeanUtil.copyProperties(requestParam, CouponTaskDO.class);
        couponTaskDO.setBatchId(IdUtil.getSnowflakeNextId());
        couponTaskDO.setOperatorId(Long.parseLong(UserContext.getUserId()));
        couponTaskDO.setShopNumber(UserContext.getShopNumber());
        couponTaskDO.setStatus(
                Objects.equals(requestParam.getSendType(), CouponTaskSendTypeEnum.IMMEDIATE.getType())
                        ? CouponTaskStatusEnum.IN_PROGRESS.getStatus()
                        : CouponTaskStatusEnum.PENDING.getStatus()
        );

        // 通过 EasyExcel 监听器获取 Excel 中所有行数
        RowCountListener listener = new RowCountListener();
        EasyExcel.read(requestParam.getFileAddress(), listener).sheet().doRead();
        // 为什么需要统计行数？因为发送后需要比对所有优惠券是否都已发放到用户账号
        int totalRows = listener.getRowCount();
        couponTaskDO.setSendNum(totalRows);

        // 保存优惠券推送任务记录到数据库
        couponTaskMapper.insert(couponTaskDO);
    }

    private void validateRequestParam(CouponTaskCreateReqDTO requestParam) {
        // 基本非空验证
        if (requestParam.getTaskName() == null || requestParam.getFileAddress() == null
                || requestParam.getNotifyType() == null
                || requestParam.getCouponTemplateId() == null
                || requestParam.getSendType() == null) {
            throw new ClientException("优惠券任务参数不能为空");
        }

        // 文件地址格式验证
        if (!isValidExcelFile(requestParam.getFileAddress())) {
            throw new ClientException("文件地址格式不正确，仅支持Excel文件");
        }

        // 发送类型依赖验证
        if (Objects.equals(requestParam.getSendType(), CouponTaskSendTypeEnum.SCHEDULED.getType())
                && requestParam.getSendTime() == null) {
            throw new ClientException("定时发送类型必须设置发送时间");
        }
    }

    private boolean isValidExcelFile(String fileAddress) {
        return fileAddress != null && (fileAddress.endsWith(".xlsx") || fileAddress.endsWith(".xls"));
    }
}
