package com.nirvanafire.ocadmin.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void generateAndValidateToken() {
        String username = "testuser";
        Set<String> roles = Set.of("admin", "user");
        Set<String> permissions = Set.of("user:create", "user:update");

        String token = jwtTokenProvider.generateToken(username, roles, permissions);

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(username, jwtTokenProvider.getUsernameFromToken(token));
    }

    @Test
    void validateInvalidToken() {
        String invalidToken = "invalid.token.here";
        assertFalse(jwtTokenProvider.validateToken(invalidToken));
    }

    @Test
    void getUsernameFromToken() {
        String username = "admin";
        Set<String> roles = Set.of("admin");
        Set<String> permissions = Set.of("user:list");

        String token = jwtTokenProvider.generateToken(username, roles, permissions);
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        assertEquals(username, extractedUsername);
    }

    @Test
    void getRolesFromToken() {
        String username = "admin";
        Set<String> roles = Set.of("admin", "test");
        Set<String> permissions = Set.of("user:list");

        String token = jwtTokenProvider.generateToken(username, roles, permissions);
        Set<String> extractedRoles = jwtTokenProvider.getRolesFromToken(token);

        assertTrue(extractedRoles.contains("admin"));
        assertTrue(extractedRoles.contains("test"));
    }

    @Test
    void validateMalformedToken() {
        assertFalse(jwtTokenProvider.validateToken("malformed"));
        assertFalse(jwtTokenProvider.validateToken(""));
    }
}
