package com.nirvanafire.ocadmin.service;

import com.nirvanafire.ocadmin.common.exception.BusinessException;
import com.nirvanafire.ocadmin.dto.ChangePasswordDTO;
import com.nirvanafire.ocadmin.dto.ProfileDTO;
import com.nirvanafire.ocadmin.entity.SysUser;
import com.nirvanafire.ocadmin.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * ProfileService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ProfileServiceImpl profileService;

    private SysUser testUser;

    @BeforeEach
    void setUp() {
        testUser = SysUser.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .nickname("测试用户")
                .email("test@example.com")
                .phone("13800138000")
                .avatar("/avatars/default.png")
                .signature("个人签名")
                .notifyApproval(true)
                .notifyAttendance(true)
                .notifyAnnouncement(true)
                .notifyEmail(false)
                .notifySms(false)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
    }

    @Test
    void getProfile_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        ProfileDTO result = profileService.getProfile("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("测试用户", result.getNickname());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("13800138000", result.getPhone());
        assertEquals("个人签名", result.getSignature());
    }

    @Test
    void getProfile_UserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> {
            profileService.getProfile("nonexistent");
        });
    }

    @Test
    void updateProfile_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(SysUser.class))).thenReturn(testUser);

        ProfileDTO updateDTO = new ProfileDTO();
        updateDTO.setNickname("新昵称");
        updateDTO.setSignature("新签名");
        updateDTO.setNotifyApproval(false);

        ProfileDTO result = profileService.updateProfile("testuser", updateDTO);

        assertNotNull(result);
        verify(userRepository).save(any(SysUser.class));
    }

    @Test
    void changePassword_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");

        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setOldPassword("oldPassword");
        dto.setNewPassword("newPassword");
        dto.setConfirmPassword("newPassword");

        assertDoesNotThrow(() -> profileService.changePassword("testuser", dto));
        verify(userRepository).save(any(SysUser.class));
    }

    @Test
    void changePassword_WrongOldPassword() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setOldPassword("wrongPassword");
        dto.setNewPassword("newPassword");
        dto.setConfirmPassword("newPassword");

        assertThrows(BusinessException.class, () -> {
            profileService.changePassword("testuser", dto);
        });
    }

    @Test
    void changePassword_PasswordMismatch() {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setOldPassword("oldPassword");
        dto.setNewPassword("newPassword1");
        dto.setConfirmPassword("newPassword2");

        assertThrows(BusinessException.class, () -> {
            profileService.changePassword("testuser", dto);
        });
    }

    @Test
    void changePassword_SameAsOldPassword() {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setOldPassword("oldPassword");
        dto.setNewPassword("oldPassword");
        dto.setConfirmPassword("oldPassword");

        assertThrows(BusinessException.class, () -> {
            profileService.changePassword("testuser", dto);
        });
    }

    @Test
    void bindPhone_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByPhoneAndIdNot("13900139000", 1L)).thenReturn(false);

        assertDoesNotThrow(() -> {
            profileService.bindPhone("testuser", "13900139000", "123456");
        });
    }

    @Test
    void bindPhone_WrongVerifyCode() {
        assertThrows(BusinessException.class, () -> {
            profileService.bindPhone("testuser", "13900139000", "wrongcode");
        });
    }

    @Test
    void bindPhone_PhoneAlreadyUsed() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByPhoneAndIdNot("13900139000", 1L)).thenReturn(true);

        assertThrows(BusinessException.class, () -> {
            profileService.bindPhone("testuser", "13900139000", "123456");
        });
    }
}
