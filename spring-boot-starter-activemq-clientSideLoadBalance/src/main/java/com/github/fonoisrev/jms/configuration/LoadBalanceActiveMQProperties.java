package com.github.fonoisrev.jms.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * type-safe config properties
 *
 * @author fonoisrev
 */
@ConfigurationProperties(prefix = "activemq.loadbalance")
public class LoadBalanceActiveMQProperties {

    private List<String> urls;

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public List<String> getUrls() {
        return this.urls;
    }

}
