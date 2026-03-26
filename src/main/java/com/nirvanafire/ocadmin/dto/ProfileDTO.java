package com.nirvanafire.ocadmin.dto;

import lombok.Data;

/**
 * 个人中心DTO
 */
@Data
public class ProfileDTO {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private String signature;
    
    // 消息通知设置
    private Boolean notifyApproval;
    private Boolean notifyAttendance;
    private Boolean notifyAnnouncement;
    private Boolean notifyEmail;
    private Boolean notifySms;
}
