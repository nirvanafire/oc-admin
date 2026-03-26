package com.nirvanafire.ocadmin.controller;

import com.nirvanafire.ocadmin.dto.UserImportDTO;
import com.nirvanafire.ocadmin.service.UserImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用户导入控制器
 */
@RestController
@RequestMapping("/api/users/import")
@RequiredArgsConstructor
public class UserImportController {

    private final UserImportService userImportService;

    /**
     * 导入用户
     */
    @PostMapping
    public ResponseEntity<List<UserImportDTO>> importUsers(
            @RequestParam("file") MultipartFile file) {
        List<UserImportDTO> results = userImportService.importUsers(file);
        return ResponseEntity.ok(results);
    }
}
