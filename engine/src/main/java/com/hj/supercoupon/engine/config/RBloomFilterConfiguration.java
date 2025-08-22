package com.hj.supercoupon.engine.config;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 布隆过滤器配置类
 */
@Configuration
public class RBloomFilterConfiguration {

    /**
     * 优惠券查询缓存穿透布隆过滤器
     */
    @Bean
    public RBloomFilter<String> couponTemplateQueryBloomFilter(RedissonClient redissonClient, @Value("${framework.cache.redis.prefix:}") String cachePrefix) {
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(cachePrefix +"couponTemplateQueryBloomFilter");
        bloomFilter.tryInit(640L, 0.001);
        return bloomFilter;
    }
    /**
     * 防止取消提醒缓存穿透的布隆过滤器
     */
    @Bean
    public RBloomFilter<String> cancelRemindBloomFilter(RedissonClient redissonClient, @Value("${framework.cache.redis.prefix:}") String cachePrefix) {
         RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter(cachePrefix + "cancelRemindBloomFilter");
         bloomFilter.tryInit(640L, 0.001);
         return bloomFilter;
    }
}
