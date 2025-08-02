package com.hj.supercoupon.framework.config;
import com.hj.supercoupon.framework.idempotent.NoDuplicateSubmitAspect;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;

/**
 * 幂等组件相关配置类
 */
public class IdempotentConfiguration {

    /**
     * 防止用户重复提交表单信息切面控制器
     */
    @Bean
    public NoDuplicateSubmitAspect noDuplicateSubmitAspect(RedissonClient redissonClient) {
        return new NoDuplicateSubmitAspect(redissonClient);
    }
}
