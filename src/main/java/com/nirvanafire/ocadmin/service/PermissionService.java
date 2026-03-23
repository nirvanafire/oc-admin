package com.nirvanafire.ocadmin.service;

import com.nirvanafire.ocadmin.dto.PermissionDTO;
import com.nirvanafire.ocadmin.dto.PermissionRequest;

import java.util.List;

public interface PermissionService {
    PermissionDTO create(PermissionRequest request);

    PermissionDTO update(Long id, PermissionRequest request);

    void delete(Long id);

    PermissionDTO getById(Long id);

    List<PermissionDTO> getTree();        // 分类树形结构

    List<PermissionDTO> getFlatList();    // 扁平列表
}
