package com.nirvanafire.ocadmin.service;

import com.nirvanafire.ocadmin.dto.TaskDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TaskService {
    TaskDTO create(Long userId, String username, TaskDTO dto);
    TaskDTO update(Long userId, Long id, TaskDTO dto);
    void delete(Long userId, Long id);
    Page<TaskDTO> list(Long userId, String status, Pageable pageable);
    TaskDTO get(Long id);
    TaskDTO moveBoard(Long userId, Long id, String column);
    TaskDTO assign(Long userId, Long id, Long assigneeId, String assigneeName);
}
