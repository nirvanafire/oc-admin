package com.nirvanafire.ocadmin.entity;

import java.io.Serializable;
import java.util.Objects;

public class SysDeptRoleId implements Serializable {

    private Long deptId;
    private Long roleId;

    public SysDeptRoleId() {}

    public SysDeptRoleId(Long deptId, Long roleId) {
        this.deptId = deptId;
        this.roleId = roleId;
    }

    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }

    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SysDeptRoleId that = (SysDeptRoleId) o;
        return Objects.equals(deptId, that.deptId) && Objects.equals(roleId, that.roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deptId, roleId);
    }
}
