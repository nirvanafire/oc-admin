package com.nirvanafire.ocadmin.security;

import com.nirvanafire.ocadmin.entity.SysUser;
import com.nirvanafire.ocadmin.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CustomUserDetailsServiceTest {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void loadUserByUsernameSuccess() {
        UserDetails userDetails = userDetailsService.loadUserByUsername("admin");

        assertNotNull(userDetails);
        assertEquals("admin", userDetails.getUsername());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
    }

    @Test
    void loadUserByUsernameNotFound() {
        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("nonexistent");
        });
    }

    @Test
    void getUserRoles() {
        SysUser user = userRepository.findByUsername("admin").orElse(null);
        assertNotNull(user);

        Set<String> roles = userDetailsService.getUserRoles(user);

        assertNotNull(roles);
        assertTrue(roles.contains("admin"));
    }

    @Test
    void getUserPermissions() {
        SysUser user = userRepository.findByUsername("admin").orElse(null);
        assertNotNull(user);

        Set<String> permissions = userDetailsService.getUserPermissions(user);

        assertNotNull(permissions);
        assertTrue(permissions.contains("user:list"));
    }

    @Test
    void loadUserHasAuthorities() {
        UserDetails userDetails = userDetailsService.loadUserByUsername("admin");

        assertNotNull(userDetails.getAuthorities());
        assertFalse(userDetails.getAuthorities().isEmpty());

        // 检查包含角色和权限
        boolean hasRoleAdmin = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_admin"));
        assertTrue(hasRoleAdmin);
    }
}
