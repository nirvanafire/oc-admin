package com.nirvanafire.ocadmin.enums;

/**
 * 公告类型枚举
 */
public enum AnnouncementType {
    NEWS("新闻"),
    NOTICE("通知"),
    ACTIVITY("活动");

    private final String description;

    AnnouncementType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
