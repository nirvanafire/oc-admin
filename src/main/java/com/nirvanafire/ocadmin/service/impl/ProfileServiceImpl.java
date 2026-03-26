package com.nirvanafire.ocadmin.service.impl;

import com.nirvanafire.ocadmin.common.exception.BusinessException;
import com.nirvanafire.ocadmin.dto.ChangePasswordDTO;
import com.nirvanafire.ocadmin.dto.ProfileDTO;
import com.nirvanafire.ocadmin.entity.SysUser;
import com.nirvanafire.ocadmin.repository.UserRepository;
import com.nirvanafire.ocadmin.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 个人中心服务实现
 */
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ProfileDTO getProfile(String username) {
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return toDTO(user);
    }

    @Override
    @Transactional
    public ProfileDTO updateProfile(String username, ProfileDTO dto) {
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        if (StringUtils.hasText(dto.getNickname())) {
            user.setNickname(dto.getNickname());
        }
        if (StringUtils.hasText(dto.getSignature())) {
            user.setSignature(dto.getSignature());
        }
        if (StringUtils.hasText(dto.getEmail())) {
            user.setEmail(dto.getEmail());
        }
        if (StringUtils.hasText(dto.getPhone())) {
            user.setPhone(dto.getPhone());
        }
        if (StringUtils.hasText(dto.getAvatar())) {
            user.setAvatar(dto.getAvatar());
        }
        
        // 更新通知设置
        if (dto.getNotifyApproval() != null) {
            user.setNotifyApproval(dto.getNotifyApproval());
        }
        if (dto.getNotifyAttendance() != null) {
            user.setNotifyAttendance(dto.getNotifyAttendance());
        }
        if (dto.getNotifyAnnouncement() != null) {
            user.setNotifyAnnouncement(dto.getNotifyAnnouncement());
        }
        if (dto.getNotifyEmail() != null) {
            user.setNotifyEmail(dto.getNotifyEmail());
        }
        if (dto.getNotifySms() != null) {
            user.setNotifySms(dto.getNotifySms());
        }
        
        user = userRepository.save(user);
        return toDTO(user);
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordDTO dto) {
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        // 验证原密码
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        
        // 验证新密码和确认密码
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("新密码与确认密码不一致");
        }
        
        // 不能与原密码相同
        if (dto.getNewPassword().equals(dto.getOldPassword())) {
            throw new BusinessException("新密码不能与原密码相同");
        }
        
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void bindPhone(String username, String phone, String verifyCode) {
        // TODO: 验证码校验逻辑（需要集成短信服务）
        // 这里暂时简化处理，实际需要校验验证码
        if (verifyCode == null || !verifyCode.equals("123456")) {
            throw new BusinessException("验证码错误或已过期");
        }
        
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        // 检查手机号是否已被使用
        if (userRepository.existsByPhoneAndIdNot(phone, user.getId())) {
            throw new BusinessException("该手机号已被其他用户绑定");
        }
        
        user.setPhone(phone);
        userRepository.save(user);
    }

    private ProfileDTO toDTO(SysUser user) {
        ProfileDTO dto = new ProfileDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAvatar(user.getAvatar());
        dto.setSignature(user.getSignature());
        dto.setNotifyApproval(user.getNotifyApproval());
        dto.setNotifyAttendance(user.getNotifyAttendance());
        dto.setNotifyAnnouncement(user.getNotifyAnnouncement());
        dto.setNotifyEmail(user.getNotifyEmail());
        dto.setNotifySms(user.getNotifySms());
        return dto;
    }
}
