package com.hj.supercoupon.merchant.admin.config;


import com.hj.supercoupon.merchant.admin.common.context.UserContext;
import com.hj.supercoupon.merchant.admin.common.context.UserInfoDTO;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 用户相关配置类
 */
@Configuration
public class UserConfiguration implements WebMvcConfigurer {

    /**
     * 用户信息传输拦截器
     */
    @Bean
    public UserTransmitInterceptor userTransmitInterceptor() {
        return new UserTransmitInterceptor();
    }

    /**
     * 添加用户信息传递过滤器至相关路径拦截
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userTransmitInterceptor())
                .addPathPatterns("/**");
    }

    /**
     * 用户信息传输拦截器
     */
    static class UserTransmitInterceptor implements HandlerInterceptor {

        @Override
        public boolean preHandle(@Nullable HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable Object handler) throws Exception {
            // 用户属于非核心功能，这里先通过模拟的形式代替。后续如果需要后管展示，会重构该代码
            UserInfoDTO userInfoDTO = new UserInfoDTO("1810518709471555585", "pdd45305558318", 1810714735922956666L);
            UserContext.setUser(userInfoDTO);
            return true;
        }

        @Override
        public void afterCompletion(@Nullable HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable Object handler, Exception exception) throws Exception {
            UserContext.removeUser();
        }
    }
}
