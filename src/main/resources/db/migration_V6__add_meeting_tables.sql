-- 迁移脚本：创建会议管理模块相关表
-- 执行时间: 2026-03-26

-- 会议室表
CREATE TABLE IF NOT EXISTS meeting_room (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '会议室名称',
    location VARCHAR(100) COMMENT '位置',
    capacity INT COMMENT '容量',
    equipment TEXT COMMENT '设备',
    enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    creator_id BIGINT COMMENT '创建人ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会议室表';

-- 会议预约表
CREATE TABLE IF NOT EXISTS meeting_reservation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '会议标题',
    description TEXT COMMENT '会议描述',
    room_id BIGINT NOT NULL COMMENT '会议室ID',
    room_name VARCHAR(50) COMMENT '会议室名称',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    organizer_id BIGINT NOT NULL COMMENT '组织者ID',
    organizer_name VARCHAR(100) COMMENT '组织者姓名',
    status VARCHAR(20) DEFAULT 'SCHEDULED' COMMENT '状态',
    checkin_code VARCHAR(10) COMMENT '签到码',
    checkin_count INT DEFAULT 0 COMMENT '签到人数',
    attachment_urls TEXT COMMENT '附件URL',
    minutes TEXT COMMENT '会议纪要',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_room_id (room_id),
    INDEX idx_organizer_id (organizer_id),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会议预约表';

-- 参会人表
CREATE TABLE IF NOT EXISTS meeting_attendee (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_id BIGINT NOT NULL COMMENT '会议预约ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    user_name VARCHAR(100) COMMENT '用户姓名',
    is_checked_in TINYINT(1) DEFAULT 0 COMMENT '是否签到',
    checkin_time DATETIME COMMENT '签到时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_reservation_id (reservation_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='参会人表';
