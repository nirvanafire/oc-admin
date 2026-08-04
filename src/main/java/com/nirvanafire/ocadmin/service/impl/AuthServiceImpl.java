package com.nirvanafire.ocadmin.service.impl;

import com.nirvanafire.ocadmin.config.RedisConfig;
import com.nirvanafire.ocadmin.dto.LoginRequest;
import com.nirvanafire.ocadmin.dto.LoginResponse;
import com.nirvanafire.ocadmin.dto.UserDTO;
import com.nirvanafire.ocadmin.entity.SysUser;
import com.nirvanafire.ocadmin.repository.UserRepository;
import com.nirvanafire.ocadmin.security.CustomUserDetailsService;
import com.nirvanafire.ocadmin.security.JwtTokenProvider;
import com.nirvanafire.ocadmin.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final CustomUserDetailsService userDetailsService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("尝试登录，用户名: {}", request.getUsername());

        // 先检查用户是否存在
        var userOpt = userRepository.findByUsername(request.getUsername());
        if (userOpt.isEmpty()) {
            log.error("用户不存在: {}", request.getUsername());
            throw new RuntimeException("用户名或密码错误");
        }

        SysUser user = userOpt.get();
        log.info("用户存在: {}, enabled: {}, roles数量: {}, password_hash: {}",
            user.getUsername(), user.getEnabled(), user.getRoles().size(), user.getPassword());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Set<String> roles = userDetailsService.getUserRoles(user);
        Set<String> permissions = userDetailsService.getUserPermissions(user);

        String token = jwtTokenProvider.generateToken(user.getUsername(), roles, permissions);

        // 缓存用户信息
        redisTemplate.opsForValue().set(
                RedisConfig.USER_CACHE_PREFIX + user.getUsername(),
                user,
                Duration.ofHours(1)
        );

        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    @Override
    public void logout(String token) {
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 将token加入黑名单
            String blacklistKey = RedisConfig.TOKEN_BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(blacklistKey, true, Duration.ofHours(2));
            
            // 清除用户缓存
            String username = jwtTokenProvider.getUsernameFromToken(token);
            redisTemplate.delete(RedisConfig.USER_CACHE_PREFIX + username);
        }
        SecurityContextHolder.clearContext();
    }

    @Override
    public UserDTO getCurrentUser(String username) {
        SysUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAvatar(user.getAvatar());
        dto.setEnabled(user.getEnabled());
        dto.setRoleIds(user.getRoles().stream().map(r -> r.getId()).collect(Collectors.toSet()));
        dto.setRoles(userDetailsService.getUserRoles(user));
        dto.setPermissions(userDetailsService.getUserPermissions(user));
        return dto;
    }
}
