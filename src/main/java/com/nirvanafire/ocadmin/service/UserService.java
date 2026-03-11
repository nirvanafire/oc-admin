package com.nirvanafire.ocadmin.service;

import com.nirvanafire.ocadmin.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserDTO create(UserDTO dto);
    UserDTO update(Long id, UserDTO dto);
    void delete(Long id);
    UserDTO getById(Long id);
    Page<UserDTO> list(Pageable pageable);
    Page<UserDTO> list(Pageable pageable, String username);
}
