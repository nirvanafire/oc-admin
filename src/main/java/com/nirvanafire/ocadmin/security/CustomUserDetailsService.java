package com.nirvanafire.ocadmin.security;

import com.nirvanafire.ocadmin.entity.SysPermission;
import com.nirvanafire.ocadmin.entity.SysRole;
import com.nirvanafire.ocadmin.entity.SysUser;
import com.nirvanafire.ocadmin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        
        // 添加角色
        for (SysRole role : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode()));
            
            // 添加权限
            for (SysPermission permission : role.getPermissions()) {
                authorities.add(new SimpleGrantedAuthority(permission.getCode()));
            }
        }

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
