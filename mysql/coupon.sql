-- =====================================================
-- 优惠券系统数据库建表脚本
-- =====================================================
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

USE huanshengxian;

-- =====================================================
-- 12. 优惠券表
-- =====================================================
CREATE TABLE IF NOT EXISTS coupons (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '优惠券ID',
    name VARCHAR(100) NOT NULL COMMENT '优惠券名称',
    type VARCHAR(20) NOT NULL COMMENT '优惠券类型: FULL_REDUCTION-满减券, DISCOUNT-折扣券, NO_THRESHOLD-无门槛券',
    discount_value DECIMAL(10, 2) NOT NULL COMMENT '面额/折扣值: 满减券为减免金额, 折扣券为折扣率(如8.5表示85折), 无门槛券为减免金额',
    min_amount DECIMAL(10, 2) DEFAULT 0.00 COMMENT '使用门槛金额: 满减券需要满足的最低订单金额, 无门槛券和折扣券为0',
    total_count INT NOT NULL DEFAULT 0 COMMENT '发放总量',
    received_count INT NOT NULL DEFAULT 0 COMMENT '已领取数量',
    used_count INT NOT NULL DEFAULT 0 COMMENT '已使用数量',
    start_time DATETIME NOT NULL COMMENT '有效期开始时间',
    end_time DATETIME NOT NULL COMMENT '有效期结束时间',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    description VARCHAR(500) COMMENT '优惠券描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at TIMESTAMP NULL COMMENT '软删除时间',
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_start_end (start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券表';

-- =====================================================
-- 13. 用户优惠券关联表
-- =====================================================
CREATE TABLE IF NOT EXISTS user_coupons (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户优惠券ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    coupon_id BIGINT NOT NULL COMMENT '优惠券ID',
    order_id BIGINT COMMENT '使用该优惠券的订单ID',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-未使用, 1-已使用, 2-已过期',
    received_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
    used_at TIMESTAMP NULL COMMENT '使用时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at TIMESTAMP NULL COMMENT '软删除时间',
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (coupon_id) REFERENCES coupons(id),
    UNIQUE KEY uk_user_coupon (user_id, coupon_id),
    INDEX idx_user (user_id),
    INDEX idx_coupon (coupon_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户优惠券关联表';

-- =====================================================
-- 插入优惠券演示数据
-- =====================================================
INSERT INTO coupons (name, type, discount_value, min_amount, total_count, received_count, used_count, start_time, end_time, status, description) VALUES
('新人专享满减券', 'FULL_REDUCTION', 10.00, 50.00, 1000, 56, 23, '2024-01-01 00:00:00', '2025-12-31 23:59:59', 1, '新用户注册专享，满50元减10元'),
('全场85折券', 'DISCOUNT', 8.5, 100.00, 500, 120, 45, '2024-06-01 00:00:00', '2025-06-30 23:59:59', 1, '全场商品85折优惠，满100元可用'),
('无门槛5元券', 'NO_THRESHOLD', 5.00, 0.00, 2000, 340, 89, '2024-01-01 00:00:00', '2025-12-31 23:59:59', 1, '无门槛使用，直接抵扣5元'),
('生鲜满减券', 'FULL_REDUCTION', 20.00, 100.00, 800, 200, 67, '2024-03-01 00:00:00', '2025-09-30 23:59:59', 1, '生鲜专区满100元减20元'),
('限时9折券', 'DISCOUNT', 9.0, 50.00, 300, 80, 12, '2024-01-01 00:00:00', '2024-06-30 23:59:59', 0, '限时9折优惠，已过期禁用');
