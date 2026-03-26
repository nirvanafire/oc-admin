package com.nirvanafire.ocadmin.service;

import com.nirvanafire.ocadmin.dto.DeptDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DeptService {

    DeptDTO create(DeptDTO dto);

    DeptDTO update(Long id, DeptDTO dto);

    void delete(Long id);

    DeptDTO getById(Long id);

    Page<DeptDTO> list(Pageable pageable, String deptName);

    List<DeptDTO> getTree();

    List<DeptDTO> getAll();

    List<DeptDTO> getChildren(Long parentId);

    List<Long> getUserIdsByDeptId(Long deptId);

    List<Long> getRoleIdsByDeptId(Long deptId);

    void addUserToDept(Long deptId, Long userId);

    void removeUserFromDept(Long deptId, Long userId);

    void addRoleToDept(Long deptId, Long roleId);

    void removeRoleFromDept(Long deptId, Long roleId);
}
