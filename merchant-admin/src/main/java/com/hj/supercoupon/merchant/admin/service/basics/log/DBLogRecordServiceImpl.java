package com.hj.supercoupon.merchant.admin.service.basics.log;

import cn.hutool.core.util.StrUtil;
import com.hj.supercoupon.merchant.admin.common.context.UserContext;
import com.hj.supercoupon.merchant.admin.dao.entity.CouponTemplateLogDO;
import com.hj.supercoupon.merchant.admin.dao.mapper.CouponTemplateLogMapper;
import com.mzt.logapi.beans.LogRecord;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.ILogRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 保存操作日志新增到数据库
 * @Author web-cat
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DBLogRecordServiceImpl implements ILogRecordService {
    private final CouponTemplateLogMapper couponTemplateLogMapper;

    @Override
    public void record(LogRecord logRecord) {
        try {
            switch (logRecord.getType()) {
                case "CouponTemplate": {
                    CouponTemplateLogDO couponTemplateLogDO = CouponTemplateLogDO.builder()
                            .couponTemplateId(logRecord.getBizNo())
                            .shopNumber(UserContext.getShopNumber())
                            .operatorId(UserContext.getUserId())
                            .operationLog(logRecord.getAction())
                            .originalData(Optional.ofNullable(LogRecordContext.getVariable("originalData")).map(Object::toString).orElse(null))
                            .modifiedData(StrUtil.isBlank(logRecord.getExtra()) ? null : logRecord.getExtra())
                            .build();
                    couponTemplateLogMapper.insert(couponTemplateLogDO);
                }
            }
        } catch (Exception ex) {
            log.error("记录[{}]操作日志失败", logRecord.getType(), ex);
        }
    }

    @Override
    public List<LogRecord> queryLog(String bizNo, String type) {
        return List.of();
    }

    @Override
    public List<LogRecord> queryLogByBizNo(String bizNo, String type, String subType) {
        return List.of();
    }
}
