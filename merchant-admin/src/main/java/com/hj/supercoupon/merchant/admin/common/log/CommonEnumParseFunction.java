package com.hj.supercoupon.merchant.admin.common.log;

import cn.hutool.core.util.StrUtil;
import com.hj.supercoupon.merchant.admin.common.enums.DiscountTargetEnum;
import com.hj.supercoupon.merchant.admin.common.enums.DiscountTypeEnum;
import com.mzt.logapi.service.IParseFunction;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * 操作日志组件解析枚举值对应描述信息
 * @Author web-cat
 */
@Component
public class CommonEnumParseFunction implements IParseFunction {
    public static final String DISCOUNT_TARGET_ENUM_NAME = DiscountTargetEnum.class.getSimpleName();
    private static final String DISCOUNT_TYPE_ENUM_NAME = DiscountTypeEnum.class.getSimpleName();
    @Override
    public String functionName() {
        return "COMMON_ENUM_PARSE";
    }
    @Override
    public String apply(Object value) {
        try {
            List<String> parts = StrUtil.split(value.toString(), "_");
            if (parts.size() != 2) {
                throw new IllegalArgumentException("格式错误，需要 '枚举类_具体值' 的形式。");
            }

            String enumClassName = parts.get(0);
            int enumValue = Integer.parseInt(parts.get(1));

            return findEnumValueByName(enumClassName, enumValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("第二个下划线后面的值需要是整数。", e);
        }
    }

    private String findEnumValueByName(String enumClassName, int enumValue) {
        if (DISCOUNT_TARGET_ENUM_NAME.equals(enumClassName)) {
            return DiscountTargetEnum.findValueByType(enumValue);
        } else if (DISCOUNT_TYPE_ENUM_NAME.equals(enumClassName)) {
            return DiscountTypeEnum.findValueByType(enumValue);
        } else {
            throw new IllegalArgumentException("未知的枚举类名: " + enumClassName);
        }
    }
}
