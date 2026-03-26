package com.nirvanafire.ocadmin.enums;

/**
 * 请假类型枚举
 */
public enum LeaveType {
    ANNUAL("年假", 15),
    SICK("病假", 0),
    PERSONAL("事假", 0),
    MARRIAGE("婚假", 10),
    MATERNITY("产假", 98),
    PATERNITY("陪产假", 7),
    FUNERAL("丧假", 3);

    private final String description;
    private final int defaultDays;

    LeaveType(String description, int defaultDays) {
        this.description = description;
        this.defaultDays = defaultDays;
    }

    public String getDescription() {
        return description;
    }

    public int getDefaultDays() {
        return defaultDays;
    }
}
