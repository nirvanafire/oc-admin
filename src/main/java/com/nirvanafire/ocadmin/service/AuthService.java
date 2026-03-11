package com.nirvanafire.ocadmin.service;

import com.nirvanafire.ocadmin.dto.LoginRequest;
import com.nirvanafire.ocadmin.dto.LoginResponse;
import com.nirvanafire.ocadmin.dto.UserDTO;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    void logout(String token);
    UserDTO getCurrentUser(String username);
}
