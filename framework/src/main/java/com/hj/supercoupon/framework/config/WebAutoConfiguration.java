package com.hj.supercoupon.framework.config;

import com.hj.supercoupon.framework.web.GlobalExceptionHandler;
import org.springframework.context.annotation.Bean;

/**
 * Web 组件自动装配
 *@Author web-cat
*/
public class WebAutoConfiguration {
    /**
     * 构建全局异常拦截器组件 Bean
     */
    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}
