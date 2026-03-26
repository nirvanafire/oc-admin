-- 迁移脚本：创建资产管理模块相关表
-- 执行时间: 2026-03-26

-- 资产表
CREATE TABLE IF NOT EXISTS asset (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_code VARCHAR(50) NOT NULL UNIQUE COMMENT '资产编号',
    name VARCHAR(100) NOT NULL COMMENT '资产名称',
    spec VARCHAR(200) COMMENT '规格',
    asset_type VARCHAR(50) COMMENT '资产类型',
    purchase_date DATE COMMENT '购入日期',
    value DECIMAL(12,2) COMMENT '价值',
    status VARCHAR(20) DEFAULT 'IDLE' COMMENT '状态: IDLE-闲置, IN_USE-使用中, BORROWED-已借出, MAINTENANCE-维修中, SCRAP-已报废',
    current_user_id BIGINT COMMENT '当前使用人ID',
    current_user_name VARCHAR(100) COMMENT '当前使用人',
    current_dept_id BIGINT COMMENT '当前所属部门ID',
    current_dept_name VARCHAR(100) COMMENT '当前所属部门',
    storage_location VARCHAR(200) COMMENT '存放地点',
    remark TEXT COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_asset_code (asset_code),
    INDEX idx_status (status),
    INDEX idx_current_user_id (current_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产表';

-- 资产申请表
CREATE TABLE IF NOT EXISTS asset_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    asset_id BIGINT NOT NULL COMMENT '资产ID',
    request_type VARCHAR(20) NOT NULL COMMENT '申请类型: BORROW-领用, RETURN-归还, TRANSFER-调拨, SCRAP-报废',
    user_id BIGINT NOT NULL COMMENT '申请人ID',
    user_name VARCHAR(100) COMMENT '申请人姓名',
    target_dept_id BIGINT COMMENT '目标部门ID',
    target_dept_name VARCHAR(100) COMMENT '目标部门名称',
    reason TEXT COMMENT '原因',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态',
    approval_instance_id VARCHAR(64) COMMENT '审批流程实例ID',
    reject_reason VARCHAR(500) COMMENT '拒绝原因',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    processed_at DATETIME COMMENT '处理时间',
    INDEX idx_asset_id (asset_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产申请表';
