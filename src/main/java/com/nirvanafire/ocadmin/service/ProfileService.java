package com.nirvanafire.ocadmin.service;

import com.nirvanafire.ocadmin.dto.ChangePasswordDTO;
import com.nirvanafire.ocadmin.dto.ProfileDTO;

/**
 * 个人中心服务接口
 */
public interface ProfileService {
    
    /**
     * 获取当前用户资料
     */
    ProfileDTO getProfile(String username);
    
    /**
     * 更新个人资料
     */
    ProfileDTO updateProfile(String username, ProfileDTO dto);
    
    /**
     * 修改密码
     */
    void changePassword(String username, ChangePasswordDTO dto);
    
    /**
     * 绑定/更新手机号
     */
    void bindPhone(String username, String phone, String verifyCode);
}
