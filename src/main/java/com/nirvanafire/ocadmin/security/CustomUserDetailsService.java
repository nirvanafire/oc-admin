package com.nirvanafire.ocadmin.security;

import com.nirvanafire.ocadmin.entity.SysPermission;
import com.nirvanafire.ocadmin.entity.SysRole;
import com.nirvanafire.ocadmin.entity.SysUser;
import com.nirvanafire.ocadmin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("加载用户信息: {}", username);

        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        log.info("找到用户: {}, enabled: {}, accountNonLocked: {}, accountNonExpired: {}, credentialsNonExpired: {}",
            user.getUsername(), user.getEnabled(), user.getAccountNonLocked(),
            user.getAccountNonExpired(), user.getCredentialsNonExpired());

        Set<SimpleGrantedAuthority> authorities = new HashSet<>();

        // 添加角色
        for (SysRole role : user.getRoles()) {
            log.info("用户角色: {}", role.getCode());
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode()));

            // 添加权限
            for (SysPermission permission : role.getPermissions()) {
                authorities.add(new SimpleGrantedAuthority(permission.getCode()));
            }
        }

        log.info("用户权限数量: {}", authorities.size());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getEnabled(),
                user.getAccountNonExpired(),
                user.getCredentialsNonExpired(),
                user.getAccountNonLocked(),
                authorities
        );
    }

    public Set<String> getUserRoles(SysUser user) {
        return user.getRoles().stream()
                .map(SysRole::getCode)
                .collect(Collectors.toSet());
    }

    public Set<String> getUserPermissions(SysUser user) {
        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(SysPermission::getCode)
                .collect(Collectors.toSet());
    }
}
