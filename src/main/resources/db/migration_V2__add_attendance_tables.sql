-- 迁移脚本：创建考勤管理模块相关表
-- 执行时间: 2026-03-26

-- 考勤记录表
CREATE TABLE IF NOT EXISTS attendance_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    record_date DATE NOT NULL COMMENT '考勤日期',
    check_in_time DATETIME COMMENT '签到时间',
    check_out_time DATETIME COMMENT '签退时间',
    check_in_device VARCHAR(100) COMMENT '签到设备信息',
    check_out_device VARCHAR(100) COMMENT '签退设备信息',
    check_in_location VARCHAR(255) COMMENT '签到位置',
    check_out_location VARCHAR(255) COMMENT '签退位置',
    status VARCHAR(20) DEFAULT 'NORMAL' COMMENT '状态: NORMAL-正常, LATE-迟到, EARLY_LEAVE-早退, ABSENT-缺卡, LEAVE-请假, HOLIDAY-节假日',
    remark VARCHAR(500) COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_date (user_id, record_date),
    INDEX idx_record_date (record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤记录表';

-- 请假申请表
CREATE TABLE IF NOT EXISTS leave_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '申请人ID',
    leave_type VARCHAR(20) NOT NULL COMMENT '请假类型: ANNUAL-年假, SICK-病假, PERSONAL-事假, MARRIAGE-婚假, MATERNITY-产假, PATERNITY-陪产假, FUNERAL-丧假',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE NOT NULL COMMENT '结束日期',
    total_days DECIMAL(5,1) NOT NULL COMMENT '请假总天数',
    reason VARCHAR(500) COMMENT '请假原因',
    attachment_url VARCHAR(255) COMMENT '附件URL',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING-待审批, APPROVED-已批准, REJECTED-已拒绝, CANCELLED-已取消',
    approval_instance_id VARCHAR(64) COMMENT '审批流程实例ID',
    approver_id BIGINT COMMENT '审批人ID',
    approved_at DATETIME COMMENT '审批时间',
    reject_reason VARCHAR(500) COMMENT '拒绝原因',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_dates (start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请假申请表';

-- 加班申请表
CREATE TABLE IF NOT EXISTS overtime_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '申请人ID',
    overtime_date DATE NOT NULL COMMENT '加班日期',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    duration_hours DECIMAL(5,1) NOT NULL COMMENT '加班时长（小时）',
    reason VARCHAR(500) COMMENT '加班原因',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING-待审批, APPROVED-已批准, REJECTED-已拒绝, CANCELLED-已取消',
    approval_instance_id VARCHAR(64) COMMENT '审批流程实例ID',
    approver_id BIGINT COMMENT '审批人ID',
    approved_at DATETIME COMMENT '审批时间',
    reject_reason VARCHAR(500) COMMENT '拒绝原因',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_overtime_date (overtime_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='加班申请表';

-- 调休申请表
CREATE TABLE IF NOT EXISTS compensatory_leave (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '申请人ID',
    overtime_request_id BIGINT COMMENT '关联的加班申请ID',
    leave_date DATE NOT NULL COMMENT '调休日期',
    duration_hours DECIMAL(5,1) NOT NULL COMMENT '调休时长（小时）',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING-待审批, APPROVED-已批准, REJECTED-已拒绝, CANCELLED-已取消',
    approval_instance_id VARCHAR(64) COMMENT '审批流程实例ID',
    approver_id BIGINT COMMENT '审批人ID',
    approved_at DATETIME COMMENT '审批时间',
    reject_reason VARCHAR(500) COMMENT '拒绝原因',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_leave_date (leave_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调休申请表';

-- 加班调休余额表
CREATE TABLE IF NOT EXISTS overtime_balance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    year INT NOT NULL COMMENT '年份',
    total_hours DECIMAL(5,1) DEFAULT 0 COMMENT '本年总加班时长',
    used_hours DECIMAL(5,1) DEFAULT 0 COMMENT '已使用调休时长',
    available_hours DECIMAL(5,1) DEFAULT 0 COMMENT '可用调休时长',
    expired_hours DECIMAL(5,1) DEFAULT 0 COMMENT '已过期时长',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_year (user_id, year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='加班调休余额表';

-- 考勤规则表
CREATE TABLE IF NOT EXISTS attendance_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_name VARCHAR(50) NOT NULL COMMENT '规则名称',
    work_start_time TIME NOT NULL COMMENT '上班时间',
    work_end_time TIME NOT NULL COMMENT '下班时间',
    flexible_minutes INT DEFAULT 0 COMMENT '弹性时间（分钟）',
    late_threshold_minutes INT DEFAULT 15 COMMENT '迟到阈值（分钟）',
    early_leave_threshold_minutes INT DEFAULT 15 COMMENT '早退阈值（分钟）',
    min_work_hours DECIMAL(3,1) DEFAULT 8.0 COMMENT '每日最低工作时长',
    is_default TINYINT(1) DEFAULT 0 COMMENT '是否为默认规则',
    enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    remark VARCHAR(255) COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤规则表';

-- 年假余额表
CREATE TABLE IF NOT EXISTS annual_leave_balance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    year INT NOT NULL COMMENT '年份',
    total_days DECIMAL(5,1) DEFAULT 0 COMMENT '本年可休年假天数',
    used_days DECIMAL(5,1) DEFAULT 0 COMMENT '已使用天数',
    available_days DECIMAL(5,1) DEFAULT 0 COMMENT '可用天数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_year (user_id, year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='年假余额表';

-- 初始化默认考勤规则
INSERT INTO attendance_rule (rule_name, work_start_time, work_end_time, flexible_minutes, late_threshold_minutes, early_leave_threshold_minutes, min_work_hours, is_default, enabled) VALUES
('默认规则', '09:00:00', '18:00:00', 0, 15, 15, 8.0, 1, 1);
