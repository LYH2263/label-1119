-- 优惠券系统数据库脚本
-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 使用数据库
USE huanshengxian;

-- =====================================================
-- 1. 优惠券表
-- =====================================================
DROP TABLE IF EXISTS coupons;
CREATE TABLE coupons (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '优惠券ID',
    name VARCHAR(100) NOT NULL COMMENT '优惠券名称',
    type TINYINT NOT NULL COMMENT '优惠券类型: 1-满减券, 2-折扣券, 3-无门槛券',
    amount DECIMAL(10, 2) COMMENT '满减券面额/无门槛券金额',
    discount DECIMAL(3, 2) COMMENT '折扣券折扣比例(0-1)',
    min_amount DECIMAL(10, 2) DEFAULT 0 COMMENT '使用门槛金额',
    total_count INT NOT NULL DEFAULT 0 COMMENT '发放总量',
    received_count INT NOT NULL DEFAULT 0 COMMENT '已领取数量',
    used_count INT NOT NULL DEFAULT 0 COMMENT '已使用数量',
    valid_start_time DATETIME NOT NULL COMMENT '有效期开始时间',
    valid_end_time DATETIME NOT NULL COMMENT '有效期结束时间',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    description VARCHAR(500) COMMENT '优惠券描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at TIMESTAMP NULL COMMENT '删除时间(软删除)',
    INDEX idx_status (status),
    INDEX idx_valid_time (valid_start_time, valid_end_time),
    INDEX idx_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券表';

-- =====================================================
-- 2. 用户优惠券关联表
-- =====================================================
DROP TABLE IF EXISTS user_coupons;
CREATE TABLE user_coupons (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    coupon_id BIGINT NOT NULL COMMENT '优惠券ID',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-未使用, 1-已使用, 2-已过期',
    order_id BIGINT COMMENT '使用的订单ID',
    used_time TIMESTAMP NULL COMMENT '使用时间',
    received_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at TIMESTAMP NULL COMMENT '删除时间(软删除)',
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (coupon_id) REFERENCES coupons(id),
    FOREIGN KEY (order_id) REFERENCES orders(id),
    UNIQUE KEY uk_user_coupon (user_id, coupon_id, deleted_at),
    INDEX idx_user_id (user_id),
    INDEX idx_coupon_id (coupon_id),
    INDEX idx_status (status),
    INDEX idx_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户优惠券关联表';

-- =====================================================
-- 插入演示数据
-- =====================================================

-- 插入优惠券数据
INSERT INTO coupons (name, type, amount, discount, min_amount, total_count, received_count, used_count, valid_start_time, valid_end_time, status, description) VALUES
('新人专享满减券', 1, 20.00, NULL, 99.00, 1000, 156, 89, '2024-01-01 00:00:00', '2024-12-31 23:59:59', 1, '新用户专属满99减20优惠券'),
('会员专享8折券', 2, NULL, 0.80, 0.00, 500, 234, 128, '2024-01-01 00:00:00', '2024-06-30 23:59:59', 1, '全场通用8折优惠券'),
('无门槛10元券', 3, 10.00, NULL, 0.00, 2000, 1234, 567, '2024-01-01 00:00:00', '2024-03-31 23:59:59', 1, '无门槛立减10元优惠券'),
('满199减50券', 1, 50.00, NULL, 199.00, 800, 456, 234, '2024-02-01 00:00:00', '2024-02-29 23:59:59', 1, '满199元立减50元'),
('新鲜水果9折券', 2, NULL, 0.90, 50.00, 1000, 345, 178, '2024-01-15 00:00:00', '2024-03-15 23:59:59', 1, '水果专区满50享9折优惠');

-- 插入用户优惠券数据
INSERT INTO user_coupons (user_id, coupon_id, status, order_id, used_time) VALUES
(4, 1, 1, 1, '2024-02-01 10:30:00'),
(4, 2, 0, NULL, NULL),
(5, 1, 1, 2, '2024-02-01 14:20:00'),
(5, 3, 0, NULL, NULL),
(4, 5, 0, NULL, NULL);
