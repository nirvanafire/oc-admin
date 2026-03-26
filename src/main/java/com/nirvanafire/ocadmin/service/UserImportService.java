package com.nirvanafire.ocadmin.service;

import com.nirvanafire.ocadmin.dto.UserImportDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用户导入服务接口
 */
public interface UserImportService {
    
    /**
     * 导入用户（Excel格式）
     * @param file Excel文件
     * @return 导入结果列表
     */
    List<UserImportDTO> importUsers(MultipartFile file);
}
