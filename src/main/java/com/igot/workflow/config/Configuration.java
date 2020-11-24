package com.igot.workflow.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Configuration {

    @Value("${workflow.pagination.default.limit}")
    private Integer defaultLimit;

    @Value("${workflow.pagination.default.offset}")
    private Integer defaultOffset;

    @Value("${workflow.pagination.max.limit}")
    private Integer maxLimit;

    @Value("${hub.service.host}")
    private String hubServiceHost;

    @Value("${hub.profile.update}")
    private String hubProfileUpdateEndPoint;

    public Integer getDefaultLimit() {
        return defaultLimit;
    }

    public void setDefaultLimit(Integer defaultLimit) {
        this.defaultLimit = defaultLimit;
    }

    public Integer getDefaultOffset() {
        return defaultOffset;
    }

    public void setDefaultOffset(Integer defaultOffset) {
        this.defaultOffset = defaultOffset;
    }

    public Integer getMaxLimit() {
        return maxLimit;
    }

    public void setMaxLimit(Integer maxLimit) {
        this.maxLimit = maxLimit;
    }

    public String getHubServiceHost() {
        return hubServiceHost;
    }

    public void setHubServiceHost(String hubServiceHost) {
        this.hubServiceHost = hubServiceHost;
    }

    public String getHubProfileUpdateEndPoint() {
        return hubProfileUpdateEndPoint;
    }

    public void setHubProfileUpdateEndPoint(String hubProfileUpdateEndPoint) {
        this.hubProfileUpdateEndPoint = hubProfileUpdateEndPoint;
    }
}
