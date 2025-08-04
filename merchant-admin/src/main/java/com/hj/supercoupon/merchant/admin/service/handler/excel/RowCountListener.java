package com.hj.supercoupon.merchant.admin.service.handler.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import lombok.Getter;

/**
 * excel 行数统计监听器
 * @author web-cat
 */
public class RowCountListener extends AnalysisEventListener<Object> {
    @Getter
    private int rowCount = 0;

    @Override
    public void invoke(Object data, AnalysisContext context) {
        rowCount++;
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // No additional actions needed after all data is analyzed
    }
}
