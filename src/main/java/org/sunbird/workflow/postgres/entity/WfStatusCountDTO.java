package org.sunbird.workflow.postgres.entity;

public class WfStatusCountDTO {
    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public Long getStatusCount() {
        return statusCount;
    }

    public void setStatusCount(Long statusCount) {
        this.statusCount = statusCount;
    }

    private String currentStatus;
    private Long statusCount;
}
