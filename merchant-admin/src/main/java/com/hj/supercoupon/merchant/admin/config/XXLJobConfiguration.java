package com.hj.supercoupon.merchant.admin.config;

import cn.hutool.core.util.StrUtil;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Paths;

/**
 * XXL-Job 配置类
 * @author web-cat
 */
@Configuration
@ConditionalOnProperty(prefix = "xxl-job", name = "enabled", havingValue = "true", matchIfMissing = true)
public class XXLJobConfiguration {

    @Value("${xxl-job.admin.addresses:}")
    private String adminAddresses;

    @Value("${xxl-job.access-token:}")
    private String accessToken;

    @Value("${xxl-job.executor.application-name}")
    private String applicationName;

    @Value("${xxl-job.executor.ip}")
    private String ip;

    @Value("${xxl-job.executor.port}")
    private int port;

    @Value("${xxl-job.executor.log-path:}")
    private String logPath;

    @Value("${xxl-job.executor.log-retention-days}")
    private int logRetentionDays;

    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(adminAddresses);
        xxlJobSpringExecutor.setAppname(applicationName);
        xxlJobSpringExecutor.setIp(ip);
        xxlJobSpringExecutor.setPort(port);
        xxlJobSpringExecutor.setAccessToken(StrUtil.isNotEmpty(accessToken) ? accessToken : null);
        xxlJobSpringExecutor.setLogPath(StrUtil.isNotEmpty(logPath) ? logPath : Paths.get("").toAbsolutePath().getParent() + "/tmp");
        xxlJobSpringExecutor.setLogRetentionDays(logRetentionDays);
        return xxlJobSpringExecutor;
    }
}
