// CuratorConfig.java
package org.didnelpsun.config;

import lombok.Data;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "curator")
@Configuration
public class CuratorConfig{
    private int retryCount;
    private int elapsedTimeMs;
    private String connectString;
    private int sessionTimeoutMs;
    private int connectionTimeoutMs;

    @Bean(initMethod="start")
    public CuratorFramework curatorFramework() {
        return CuratorFrameworkFactory.newClient(connectString, sessionTimeoutMs, connectionTimeoutMs, new RetryNTimes(retryCount, elapsedTimeMs));
    }
}
