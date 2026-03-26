package com.nirvanafire.ocadmin.controller;

import com.nirvanafire.ocadmin.dto.TaskDTO;
import com.nirvanafire.ocadmin.entity.SysUser;
import com.nirvanafire.ocadmin.repository.UserRepository;
import com.nirvanafire.ocadmin.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<TaskDTO> create(Authentication authentication, @RequestBody TaskDTO dto) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(taskService.create(userId, authentication.getName(), dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> update(Authentication authentication, @PathVariable Long id, @RequestBody TaskDTO dto) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(taskService.update(userId, id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        Long userId = getCurrentUserId(authentication);
        taskService.delete(userId, id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Page<TaskDTO>> list(Authentication authentication, @RequestParam(required = false) String status, Pageable pageable) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(taskService.list(userId, status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.get(id));
    }

    @PutMapping("/{id}/move/{column}")
    public ResponseEntity<TaskDTO> move(Authentication authentication, @PathVariable Long id, @PathVariable String column) {
        Long userId = getCurrentUserId(authentication);
        return ResponseEntity.ok(taskService.moveBoard(userId, id, column));
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<TaskDTO> assign(Authentication authentication, @PathVariable Long id, @RequestBody java.util.Map<String, Object> body) {
        Long userId = getCurrentUserId(authentication);
        Long assigneeId = Long.valueOf(body.get("assigneeId").toString());
        String assigneeName = body.get("assigneeName").toString();
        return ResponseEntity.ok(taskService.assign(userId, id, assigneeId, assigneeName));
    }

    private Long getCurrentUserId(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName()).orElseThrow().getId();
    }
}
