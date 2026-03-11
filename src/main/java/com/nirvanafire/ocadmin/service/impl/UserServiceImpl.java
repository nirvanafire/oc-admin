package com.nirvanafire.ocadmin.service.impl;

import com.nirvanafire.ocadmin.common.exception.BusinessException;
import com.nirvanafire.ocadmin.dto.UserDTO;
import com.nirvanafire.ocadmin.entity.SysRole;
import com.nirvanafire.ocadmin.entity.SysUser;
import com.nirvanafire.ocadmin.repository.RoleRepository;
import com.nirvanafire.ocadmin.repository.UserRepository;
import com.nirvanafire.ocadmin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDTO create(UserDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        
        SysUser user = SysUser.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .nickname(dto.getNickname())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .avatar(dto.getAvatar())
                .enabled(dto.getEnabled() != null ? dto.getEnabled() : true)
                .build();
        
        if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
            Set<SysRole> roles = dto.getRoleIds().stream()
                    .map(id -> roleRepository.findById(id)
                            .orElseThrow(() -> new BusinessException("角色不存在: " + id)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }
        
        user = userRepository.save(user);
        return toDTO(user);
    }

    @Override
    @Transactional
    public UserDTO update(Long id, UserDTO dto) {
        SysUser user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        if (StringUtils.hasText(dto.getNickname())) {
            user.setNickname(dto.getNickname());
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
        if (StringUtils.hasText(dto.getPassword())) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        if (dto.getEnabled() != null) {
            user.setEnabled(dto.getEnabled());
        }
        if (dto.getRoleIds() != null) {
            Set<SysRole> roles = dto.getRoleIds().stream()
                    .map(roleId -> roleRepository.findById(roleId)
                            .orElseThrow(() -> new BusinessException("角色不存在: " + roleId)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }
        
        user = userRepository.save(user);
        return toDTO(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new BusinessException("用户不存在");
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserDTO getById(Long id) {
        SysUser user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return toDTO(user);
    }

    @Override
    public Page<UserDTO> list(Pageable pageable) {
        Page<SysUser> page = userRepository.findAll(pageable);
        List<UserDTO> list = page.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(list, pageable, page.getTotalElements());
    }

    private UserDTO toDTO(SysUser user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAvatar(user.getAvatar());
        dto.setEnabled(user.getEnabled());
        dto.setRoleIds(user.getRoles().stream().map(SysRole::getId).collect(Collectors.toSet()));
        return dto;
    }
}
