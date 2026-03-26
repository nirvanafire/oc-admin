-- 迁移脚本：创建通知公告模块相关表
-- 执行时间: 2026-03-26

-- 公告表
CREATE TABLE IF NOT EXISTS announcement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '公告标题',
    content LONGTEXT COMMENT '公告内容',
    summary VARCHAR(500) COMMENT '摘要',
    announcement_type VARCHAR(20) NOT NULL COMMENT '公告类型: NEWS-新闻, NOTICE-通知, ACTIVITY-活动',
    cover_image VARCHAR(255) COMMENT '封面图片',
    is_top TINYINT(1) DEFAULT 0 COMMENT '是否置顶',
    top_expire_time DATETIME COMMENT '置顶过期时间',
    allow_comment TINYINT(1) DEFAULT 0 COMMENT '是否允许评论',
    status VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态: DRAFT-草稿, PUBLISHED-已发布, ARCHIVED-已下架',
    publisher_id BIGINT NOT NULL COMMENT '发布人ID',
    publisher_name VARCHAR(100) COMMENT '发布人姓名',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    published_at DATETIME COMMENT '发布时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_announcement_type (announcement_type),
    INDEX idx_published_at (published_at),
    INDEX idx_is_top (is_top)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- 公告阅读记录表
CREATE TABLE IF NOT EXISTS announcement_read (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    announcement_id BIGINT NOT NULL COMMENT '公告ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    read_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_announcement_user (announcement_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告阅读记录表';

-- 公告评论表
CREATE TABLE IF NOT EXISTS announcement_comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    announcement_id BIGINT NOT NULL COMMENT '公告ID',
    user_id BIGINT NOT NULL COMMENT '评论人ID',
    user_name VARCHAR(100) COMMENT '评论人姓名',
    parent_id BIGINT COMMENT '父评论ID',
    content VARCHAR(500) NOT NULL COMMENT '评论内容',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_announcement_id (announcement_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告评论表';
