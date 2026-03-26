package com.nirvanafire.ocadmin.service.impl;

import com.nirvanafire.ocadmin.common.exception.BusinessException;
import com.nirvanafire.ocadmin.dto.UserImportDTO;
import com.nirvanafire.ocadmin.entity.SysDept;
import com.nirvanafire.ocadmin.entity.SysRole;
import com.nirvanafire.ocadmin.entity.SysUser;
import com.nirvanafire.ocadmin.repository.DeptRepository;
import com.nirvanafire.ocadmin.repository.RoleRepository;
import com.nirvanafire.ocadmin.repository.UserRepository;
import com.nirvanafire.ocadmin.service.UserImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * 用户导入服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserImportServiceImpl implements UserImportService {

    private static final String[] EXCEL_HEADERS = {
        "用户名", "密码", "昵称", "邮箱", "手机号", "部门名称", "角色名称", "备注"
    };

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DeptRepository deptRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public List<UserImportDTO> importUsers(MultipartFile file) {
        List<UserImportDTO> results = new ArrayList<>();
        
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".xlsx")) {
            throw new BusinessException("请上传.xlsx格式的Excel文件");
        }
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getLastRowNum() < 1) {
                throw new BusinessException("Excel文件没有数据");
            }
            
            // 验证表头
            Row headerRow = sheet.getRow(0);
            validateHeader(headerRow);
            
            // 从第二行开始读取数据
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                UserImportDTO dto = parseRow(row, i);
                
                if (dto == null) {
                    continue; // 跳过空行
                }
                
                // 执行导入
                try {
                    importUser(dto);
                    dto.setSuccess(true);
                } catch (Exception e) {
                    dto.setSuccess(false);
                    dto.setErrorMessage(e.getMessage());
                    log.error("导入第{}行数据失败: {}", i, e.getMessage());
                }
                
                results.add(dto);
            }
            
        } catch (IOException e) {
            throw new BusinessException("读取Excel文件失败: " + e.getMessage());
        }
        
        return results;
    }
    
    private void validateHeader(Row headerRow) {
        for (int i = 0; i < EXCEL_HEADERS.length; i++) {
            Cell cell = headerRow.getCell(i);
            if (cell == null) {
                throw new BusinessException("缺少表头列: " + EXCEL_HEADERS[i]);
            }
            String headerValue = getCellStringValue(cell);
            if (!EXCEL_HEADERS[i].equals(headerValue)) {
                throw new BusinessException("表头列错误，期望: " + EXCEL_HEADERS[i] + "，实际: " + headerValue);
            }
        }
    }
    
    private UserImportDTO parseRow(Row row, int rowNum) {
        UserImportDTO dto = new UserImportDTO();
        dto.setRowNum(rowNum);
        
        // 用户名（必填）
        String username = getCellStringValue(row.getCell(0));
        if (!org.springframework.util.StringUtils.hasText(username)) {
            return null; // 跳过空行
        }
        dto.setUsername(username);
        
        // 密码（必填）
        dto.setPassword(getCellStringValue(row.getCell(1)));
        
        // 昵称
        dto.setNickname(getCellStringValue(row.getCell(2)));
        
        // 邮箱
        dto.setEmail(getCellStringValue(row.getCell(3)));
        
        // 手机号
        dto.setPhone(getCellStringValue(row.getCell(4)));
        
        // 部门名称
        dto.setDeptName(getCellStringValue(row.getCell(5)));
        
        // 角色名称（逗号分隔）
        dto.setRoleNames(getCellStringValue(row.getCell(6)));
        
        // 备注
        dto.setRemark(getCellStringValue(row.getCell(7)));
        
        return dto;
    }
    
    private void importUser(UserImportDTO dto) {
        // 验证必填字段
        if (!org.springframework.util.StringUtils.hasText(dto.getUsername())) {
            throw new BusinessException("用户名不能为空");
        }
        if (!org.springframework.util.StringUtils.hasText(dto.getPassword())) {
            throw new BusinessException("密码不能为空");
        }
        
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new BusinessException("用户名已存在: " + dto.getUsername());
        }
        
        // 查找部门
        SysDept dept = null;
        if (org.springframework.util.StringUtils.hasText(dto.getDeptName())) {
            dept = deptRepository.findByName(dto.getDeptName())
                    .orElse(null);
        }
        
        // 查找角色
        Set<SysRole> roles = new HashSet<>();
        if (org.springframework.util.StringUtils.hasText(dto.getRoleNames())) {
            String[] roleNames = dto.getRoleNames().split(",");
            for (String roleName : roleNames) {
                String trimmedName = roleName.trim();
                if (org.springframework.util.StringUtils.hasText(trimmedName)) {
                    SysRole role = roleRepository.findByName(trimmedName)
                            .orElseThrow(() -> new BusinessException("角色不存在: " + trimmedName));
                    roles.add(role);
                }
            }
        }
        
        // 创建用户
        SysUser user = SysUser.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .nickname(dto.getNickname())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
        
        if (dept != null) {
            user.setDeptId(dept.getId());
        }
        
        if (!roles.isEmpty()) {
            user.setRoles(roles);
        }
        
        userRepository.save(user);
    }
    
    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                }
                yield String.valueOf((long) cell.getNumericCellValue());
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }
}
