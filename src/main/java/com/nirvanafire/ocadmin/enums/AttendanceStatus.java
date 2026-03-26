package com.nirvanafire.ocadmin.enums;

/**
 * 考勤状态枚举
 */
public enum AttendanceStatus {
    NORMAL("正常"),
    LATE("迟到"),
    EARLY_LEAVE("早退"),
    ABSENT("缺卡"),
    LEAVE("请假"),
    HOLIDAY("节假日");

    private final String description;

    AttendanceStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
