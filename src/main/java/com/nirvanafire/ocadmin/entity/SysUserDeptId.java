package com.nirvanafire.ocadmin.entity;

import java.io.Serializable;
import java.util.Objects;

public class SysUserDeptId implements Serializable {

    private Long userId;
    private Long deptId;

    public SysUserDeptId() {}

    public SysUserDeptId(Long userId, Long deptId) {
        this.userId = userId;
        this.deptId = deptId;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getDeptId() { return deptId; }
    public void setDeptId(Long deptId) { this.deptId = deptId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SysUserDeptId that = (SysUserDeptId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(deptId, that.deptId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, deptId);
    }
}
