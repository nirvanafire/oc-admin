package com.nirvanafire.ocadmin.enums;

/**
 * 申请状态枚举
 */
public enum RequestStatus {
    PENDING("待审批"),
    APPROVED("已批准"),
    REJECTED("已拒绝"),
    CANCELLED("已取消");

    private final String description;

    RequestStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
