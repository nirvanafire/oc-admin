package com.nirvanafire.ocadmin.security;

import com.nirvanafire.ocadmin.entity.SysMenu;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("加载用户信息: {}", username);

        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        Set<SimpleGrantedAuthority> authorities = new HashSet<>();

        for (SysRole role : user.getRoles()) {
            log.info("用户角色: {}", role.getCode());
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode()));

            // 从button类型菜单节点收集权限
            for (SysMenu menu : role.getMenus()) {
                if ("button".equals(menu.getMenuType()) && menu.getPermissionCode() != null) {
                    authorities.add(new SimpleGrantedAuthority(menu.getPermissionCode()));
                }
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

    @Transactional(readOnly = true)
    public Set<String> getUserPermissions(SysUser user) {
        return user.getRoles().stream()
                .flatMap(role -> role.getMenus().stream())
                .filter(menu -> "button".equals(menu.getMenuType()) && menu.getPermissionCode() != null)
                .map(SysMenu::getPermissionCode)
                .collect(Collectors.toSet());
    }
}
