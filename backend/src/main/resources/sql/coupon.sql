-- =============================================================
-- 优惠券系统数据库建表 SQL
-- 数据库：MySQL 8.0
-- 功能说明：
--   1. coupons 表：优惠券主表
--   2. user_coupons 表：用户优惠券关联表
-- =============================================================

-- -------------------------------------------------------------
-- 优惠券主表
-- 功能说明：存储优惠券模板信息
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `coupons`;
CREATE TABLE `coupons` (
  `id`              BIGINT          NOT NULL AUTO_INCREMENT            COMMENT '优惠券ID',
  `name`            VARCHAR(100)    NOT NULL                           COMMENT '优惠券名称',
  `type`            VARCHAR(50)     NOT NULL                           COMMENT '类型：FULL_REDUCTION-满减券、DISCOUNT-折扣券、NO_THRESHOLD-无门槛券',
  `value`           DECIMAL(10,2)   NOT NULL                           COMMENT '面额/折扣值',
  `min_amount`      DECIMAL(10,2)   NOT NULL DEFAULT 0.00              COMMENT '使用门槛（最低消费金额）',
  `total_count`     INT             NOT NULL                           COMMENT '发放总量',
  `received_count`  INT             NOT NULL DEFAULT 0                 COMMENT '已领取数量',
  `used_count`      INT             NOT NULL DEFAULT 0                 COMMENT '已使用数量',
  `status`          TINYINT         NOT NULL DEFAULT 1                 COMMENT '状态：1-启用、0-禁用',
  `start_time`      DATETIME        NOT NULL                           COMMENT '有效期开始时间',
  `end_time`        DATETIME        NOT NULL                           COMMENT '有效期结束时间',
  `created_at`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at`      DATETIME        NULL                               COMMENT '删除时间（软删除）',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_time_range` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券主表';

-- -------------------------------------------------------------
-- 用户优惠券关联表
-- 功能说明：存储用户领取的优惠券
-- -------------------------------------------------------------
DROP TABLE IF EXISTS `user_coupons`;
CREATE TABLE `user_coupons` (
  `id`          BIGINT          NOT NULL AUTO_INCREMENT            COMMENT 'ID',
  `user_id`     BIGINT          NOT NULL                           COMMENT '用户ID',
  `coupon_id`   BIGINT          NOT NULL                           COMMENT '优惠券ID',
  `status`      VARCHAR(50)     NOT NULL DEFAULT 'UNUSED'          COMMENT '状态：UNUSED-未使用、USED-已使用、EXPIRED-已过期',
  `order_id`    BIGINT          NULL                               COMMENT '订单ID（使用时关联）',
  `received_at` DATETIME        NOT NULL                           COMMENT '领取时间',
  `used_at`     DATETIME        NULL                               COMMENT '使用时间',
  `created_at`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at`  DATETIME        NULL                               COMMENT '删除时间（软删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_coupon` (`user_id`, `coupon_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_coupon_id` (`coupon_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户优惠券关联表';

-- -------------------------------------------------------------
-- 初始化测试数据
-- -------------------------------------------------------------
INSERT INTO `coupons` (`name`, `type`, `value`, `min_amount`, `total_count`, `start_time`, `end_time`)
VALUES
('新人专享满100减20', 'FULL_REDUCTION', 20.00, 100.00, 1000, '2024-01-01 00:00:00', '2026-12-31 23:59:59'),
('全场8折券', 'DISCOUNT', 0.80, 0.00, 500, '2024-01-01 00:00:00', '2026-12-31 23:59:59'),
('无门槛10元券', 'NO_THRESHOLD', 10.00, 0.00, 2000, '2024-01-01 00:00:00', '2026-12-31 23:59:59'),
('满200减50生鲜券', 'FULL_REDUCTION', 50.00, 200.00, 800, '2024-01-01 00:00:00', '2026-12-31 23:59:59'),
('水果专享9折券', 'DISCOUNT', 0.90, 50.00, 1000, '2024-01-01 00:00:00', '2026-12-31 23:59:59');
