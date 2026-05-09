-- 环生鲜电商平台数据库初始化脚本
-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 使用数据库
USE huanshengxian;

-- =====================================================
-- 1. 用户表
-- =====================================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    nickname VARCHAR(100) COMMENT '昵称',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    avatar VARCHAR(500) COMMENT '头像URL',
    role ENUM('USER', 'ADMIN', 'SUPPLIER') DEFAULT 'USER' COMMENT '角色',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =====================================================
-- 2. 供应商表
-- =====================================================
CREATE TABLE IF NOT EXISTS suppliers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT COMMENT '关联用户ID',
    name VARCHAR(100) NOT NULL COMMENT '供应商名称',
    contact_person VARCHAR(50) COMMENT '联系人',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    address VARCHAR(500) COMMENT '地址',
    description TEXT COMMENT '供应商描述',
    license_no VARCHAR(100) COMMENT '营业执照号',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='供应商表';

-- =====================================================
-- 3. 商品分类表
-- =====================================================
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    icon VARCHAR(500) COMMENT '分类图标',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- =====================================================
-- 4. 商品表
-- =====================================================
CREATE TABLE IF NOT EXISTS products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_id BIGINT NOT NULL COMMENT '分类ID',
    supplier_id BIGINT COMMENT '供应商ID',
    name VARCHAR(200) NOT NULL COMMENT '商品名称',
    description TEXT COMMENT '商品描述',
    price DECIMAL(10, 2) NOT NULL COMMENT '销售价格',
    original_price DECIMAL(10, 2) COMMENT '原价',
    unit VARCHAR(20) DEFAULT '斤' COMMENT '单位',
    stock INT DEFAULT 0 COMMENT '库存',
    sales INT DEFAULT 0 COMMENT '销量',
    images VARCHAR(2000) COMMENT '商品图片(JSON数组)',
    origin VARCHAR(100) COMMENT '产地',
    shelf_life VARCHAR(50) COMMENT '保质期',
    storage_method VARCHAR(200) COMMENT '存储方式',
    is_hot TINYINT DEFAULT 0 COMMENT '是否热销',
    is_new TINYINT DEFAULT 0 COMMENT '是否新品',
    is_recommend TINYINT DEFAULT 0 COMMENT '是否推荐',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-下架, 1-上架',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id),
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
    INDEX idx_category (category_id),
    INDEX idx_supplier (supplier_id),
    INDEX idx_name (name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- =====================================================
-- 5. 收货地址表
-- =====================================================
CREATE TABLE IF NOT EXISTS addresses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    receiver_name VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    receiver_phone VARCHAR(20) NOT NULL COMMENT '收货人电话',
    province VARCHAR(50) COMMENT '省份',
    city VARCHAR(50) COMMENT '城市',
    district VARCHAR(50) COMMENT '区县',
    detail_address VARCHAR(500) NOT NULL COMMENT '详细地址',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认地址',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收货地址表';

-- =====================================================
-- 6. 购物车表
-- =====================================================
CREATE TABLE IF NOT EXISTS cart_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    selected TINYINT DEFAULT 1 COMMENT '是否选中',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    UNIQUE KEY uk_user_product (user_id, product_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';

-- =====================================================
-- 7. 订单表
-- =====================================================
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '订单编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    total_amount DECIMAL(10, 2) NOT NULL COMMENT '订单总金额',
    pay_amount DECIMAL(10, 2) COMMENT '实付金额',
    freight_amount DECIMAL(10, 2) DEFAULT 0 COMMENT '运费',
    status ENUM('PENDING', 'PAID', 'SHIPPED', 'DELIVERED', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING' COMMENT '订单状态',
    receiver_name VARCHAR(50) COMMENT '收货人姓名',
    receiver_phone VARCHAR(20) COMMENT '收货人电话',
    receiver_address VARCHAR(500) COMMENT '收货地址',
    remark VARCHAR(500) COMMENT '备注',
    pay_time TIMESTAMP NULL COMMENT '支付时间',
    ship_time TIMESTAMP NULL COMMENT '发货时间',
    receive_time TIMESTAMP NULL COMMENT '收货时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_user (user_id),
    INDEX idx_order_no (order_no),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- =====================================================
-- 8. 订单项表
-- =====================================================
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(200) COMMENT '商品名称(快照)',
    product_image VARCHAR(500) COMMENT '商品图片(快照)',
    product_price DECIMAL(10, 2) COMMENT '商品单价(快照)',
    quantity INT NOT NULL COMMENT '购买数量',
    total_price DECIMAL(10, 2) COMMENT '小计金额',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    INDEX idx_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单项表';

-- =====================================================
-- 9. 消费者反馈表
-- =====================================================
CREATE TABLE IF NOT EXISTS feedbacks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT COMMENT '商品ID',
    order_id BIGINT COMMENT '订单ID',
    type ENUM('PRODUCT', 'SERVICE', 'LOGISTICS', 'OTHER') DEFAULT 'PRODUCT' COMMENT '反馈类型',
    rating INT COMMENT '评分(1-5)',
    content TEXT COMMENT '反馈内容',
    images VARCHAR(2000) COMMENT '反馈图片',
    reply TEXT COMMENT '供应商回复',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-待处理, 1-已处理',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (order_id) REFERENCES orders(id),
    INDEX idx_user (user_id),
    INDEX idx_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消费者反馈表';

-- =====================================================
-- 10. 销售数据分析表
-- =====================================================
CREATE TABLE IF NOT EXISTS sales_analytics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT COMMENT '商品ID',
    category_id BIGINT COMMENT '分类ID',
    supplier_id BIGINT COMMENT '供应商ID',
    date DATE NOT NULL COMMENT '统计日期',
    sales_count INT DEFAULT 0 COMMENT '销售数量',
    sales_amount DECIMAL(12, 2) DEFAULT 0 COMMENT '销售金额',
    order_count INT DEFAULT 0 COMMENT '订单数量',
    avg_rating DECIMAL(3, 2) COMMENT '平均评分',
    feedback_count INT DEFAULT 0 COMMENT '反馈数量',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (category_id) REFERENCES categories(id),
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
    INDEX idx_date (date),
    INDEX idx_product (product_id),
    INDEX idx_supplier (supplier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='销售数据分析表';

-- =====================================================
-- 11. 轮播图/广告表
-- =====================================================
CREATE TABLE IF NOT EXISTS banners (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) COMMENT '标题',
    image_url VARCHAR(500) NOT NULL COMMENT '图片URL',
    link_url VARCHAR(500) COMMENT '跳转链接',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轮播图表';

-- =====================================================
-- 插入演示数据
-- =====================================================

-- 插入管理员和测试用户
INSERT INTO users (username, password, nickname, phone, email, role, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', '13800000000', 'admin@huanshengxian.com', 'ADMIN', 1),
('supplier1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '阳光农场', '13800000001', 'supplier1@huanshengxian.com', 'SUPPLIER', 1),
('supplier2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '绿源果园', '13800000002', 'supplier2@huanshengxian.com', 'SUPPLIER', 1),
('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张三', '13900000001', 'user1@example.com', 'USER', 1),
('user2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李四', '13900000002', 'user2@example.com', 'USER', 1);

-- 插入供应商信息
INSERT INTO suppliers (user_id, name, contact_person, contact_phone, address, description, license_no, status) VALUES
(2, '阳光农场', '王大明', '13800000001', '山东省寿光市蔬菜基地', '专注有机蔬菜种植20年，产品通过有机认证', 'FARM2024001', 1),
(3, '绿源果园', '李小芳', '13800000002', '陕西省洛川县苹果产区', '洛川苹果原产地，新鲜直供，品质保证', 'FARM2024002', 1);

-- 插入商品分类
INSERT INTO categories (name, icon, parent_id, sort_order, status) VALUES
('新鲜蔬菜', '🥬', 0, 1, 1),
('时令水果', '🍎', 0, 2, 1),
('肉禽蛋品', '🥩', 0, 3, 1),
('海鲜水产', '🦐', 0, 4, 1),
('粮油调味', '🍚', 0, 5, 1),
('乳品烘焙', '🥛', 0, 6, 1),
('叶菜类', '🥗', 1, 1, 1),
('根茎类', '🥕', 1, 2, 1),
('茄果类', '🍅', 1, 3, 1),
('苹果梨类', '🍐', 2, 1, 1),
('柑橘类', '🍊', 2, 2, 1),
('热带水果', '🥭', 2, 3, 1);

-- 插入商品数据
INSERT INTO products (category_id, supplier_id, name, description, price, original_price, unit, stock, sales, images, origin, shelf_life, storage_method, is_hot, is_new, is_recommend, status) VALUES
(7, 1, '有机小白菜', '新鲜采摘的有机小白菜，无农药残留，口感鲜嫩', 5.80, 6.50, '斤', 500, 1280, '["/assets/products/organic-bok-choy.jpg"]', '山东寿光', '3-5天', '冷藏保存', 1, 0, 1, 1),
(7, 1, '新鲜菠菜', '富含铁元素的优质菠菜，叶片肥厚，翠绿新鲜', 4.50, 5.00, '斤', 300, 890, '["/assets/products/fresh-spinach.jpg"]', '山东寿光', '2-3天', '冷藏保存', 1, 1, 1, 1),
(8, 1, '有机胡萝卜', '天然有机种植，富含胡萝卜素，香甜可口', 3.80, 4.50, '斤', 400, 650, '["/assets/products/organic-carrot.jpg"]', '山东寿光', '7-10天', '阴凉干燥处保存', 0, 0, 1, 1),
(8, 1, '紫薯', '富含花青素的优质紫薯，香甜软糯', 4.20, 5.00, '斤', 350, 480, '["/assets/products/purple-sweet-potato.jpg"]', '山东寿光', '15-20天', '阴凉通风处保存', 0, 1, 0, 1),
(9, 1, '新鲜西红柿', '自然成熟的番茄，酸甜可口，维C丰富', 6.00, 7.00, '斤', 600, 1520, '["/assets/products/fresh-tomato.jpg"]', '山东寿光', '5-7天', '常温或冷藏保存', 1, 0, 1, 1),
(9, 1, '彩椒三色', '红黄绿三色甜椒，色泽鲜艳，营养丰富', 8.80, 10.00, '斤', 200, 320, '["/assets/products/tricolor-bell-pepper.jpg"]', '山东寿光', '7-10天', '冷藏保存', 0, 1, 1, 1),
(10, 2, '洛川红富士苹果', '正宗洛川苹果，脆甜多汁，果香浓郁', 8.90, 10.00, '斤', 800, 2350, '["/assets/products/luochuan-fuji-apple.jpg"]', '陕西洛川', '30-45天', '冷藏保存', 1, 0, 1, 1),
(10, 2, '新疆阿克苏糖心苹果', '冰糖心苹果，甜度爆表，入口即化', 12.80, 15.00, '斤', 500, 1890, '["/assets/products/aksu-sugar-apple.jpg"]', '新疆阿克苏', '30-60天', '冷藏保存', 1, 1, 1, 1),
(10, 2, '砀山酥梨', '安徽砀山特产，皮薄肉细，汁多味甜', 6.50, 8.00, '斤', 400, 980, '["/assets/products/dangshan-pear.jpg"]', '安徽砀山', '20-30天', '冷藏保存', 0, 0, 1, 1),
(11, 2, '赣南脐橙', '赣南原产地直供，果大皮薄，甜度高', 7.80, 9.00, '斤', 600, 1680, '["/assets/products/gannan-navel-orange.jpg"]', '江西赣州', '20-30天', '阴凉通风处保存', 1, 0, 1, 1),
(11, 2, '沃柑', '皮薄肉嫩，汁水丰富，酸甜适中', 9.90, 12.00, '斤', 350, 720, '["/assets/products/wogan.jpg"]', '广西武鸣', '15-20天', '冷藏保存', 0, 1, 1, 1),
(12, 2, '海南芒果', '热带阳光味道，果肉细腻，香甜无比', 15.80, 18.00, '斤', 250, 560, '["/assets/products/hainan-mango.jpg"]', '海南三亚', '5-7天', '常温催熟后冷藏', 1, 1, 1, 1);

-- 插入用户地址
INSERT INTO addresses (user_id, receiver_name, receiver_phone, province, city, district, detail_address, is_default) VALUES
(4, '张三', '13900000001', '北京市', '北京市', '朝阳区', '望京SOHO T1 1001室', 1),
(4, '张三', '13900000001', '北京市', '北京市', '海淀区', '中关村大街1号', 0),
(5, '李四', '13900000002', '上海市', '上海市', '浦东新区', '陆家嘴金融中心88号', 1);

-- 插入轮播图数据
INSERT INTO banners (title, image_url, link_url, sort_order, status) VALUES
('新鲜蔬菜 产地直供', '/assets/banners/vegetables-banner.jpg', '#/category/1', 1, 1),
('时令水果 应季尝鲜', '/assets/banners/fruits-banner.jpg', '#/category/2', 2, 1),
('响应三农 助农惠民', '/assets/banners/agriculture-banner.jpg', '#/about', 3, 1),
('会员专享 限时特惠', '/assets/banners/member-sale-banner.jpg', '#/promotion', 4, 1);

-- 插入演示订单
INSERT INTO orders (order_no, user_id, total_amount, pay_amount, freight_amount, status, receiver_name, receiver_phone, receiver_address, remark, pay_time, created_at) VALUES
('ORD20240201001', 4, 89.00, 89.00, 0, 'COMPLETED', '张三', '13900000001', '北京市朝阳区望京SOHO T1 1001室', '请放门卫处', '2024-02-01 10:30:00', '2024-02-01 10:25:00'),
('ORD20240201002', 5, 156.50, 156.50, 0, 'DELIVERED', '李四', '13900000002', '上海市浦东新区陆家嘴金融中心88号', '', '2024-02-01 14:20:00', '2024-02-01 14:15:00'),
('ORD20240202001', 4, 68.40, 68.40, 0, 'SHIPPED', '张三', '13900000001', '北京市朝阳区望京SOHO T1 1001室', '', '2024-02-02 09:00:00', '2024-02-02 08:55:00');

-- 插入订单项
INSERT INTO order_items (order_id, product_id, product_name, product_image, product_price, quantity, total_price) VALUES
(1, 7, '洛川红富士苹果', '/assets/products/luochuan-fuji-apple.jpg', 8.90, 5, 44.50),
(1, 5, '新鲜西红柿', '/assets/products/fresh-tomato.jpg', 6.00, 3, 18.00),
(1, 1, '有机小白菜', '/assets/products/organic-bok-choy.jpg', 5.80, 2, 11.60),
(2, 8, '新疆阿克苏糖心苹果', '/assets/products/aksu-sugar-apple.jpg', 12.80, 10, 128.00),
(2, 2, '新鲜菠菜', '/assets/products/fresh-spinach.jpg', 4.50, 3, 13.50),
(3, 10, '赣南脐橙', '/assets/products/gannan-navel-orange.jpg', 7.80, 5, 39.00),
(3, 3, '有机胡萝卜', '/assets/products/organic-carrot.jpg', 3.80, 3, 11.40);

-- 插入用户反馈数据
INSERT INTO feedbacks (user_id, product_id, order_id, type, rating, content, status, created_at) VALUES
(4, 7, 1, 'PRODUCT', 5, '苹果非常新鲜，又脆又甜，包装也很好，没有任何磕碰！', 1, '2024-02-03 10:00:00'),
(4, 5, 1, 'PRODUCT', 4, '西红柿口感不错，就是个头有点小', 1, '2024-02-03 10:05:00'),
(5, 8, 2, 'PRODUCT', 5, '糖心苹果名不虚传，真的是冰糖心，全家都爱吃！', 1, '2024-02-04 15:30:00'),
(4, 10, 3, 'LOGISTICS', 4, '物流速度很快，但希望包装能再结实一些', 0, '2024-02-05 09:00:00');

-- 插入销售分析数据
INSERT INTO sales_analytics (product_id, category_id, supplier_id, date, sales_count, sales_amount, order_count, avg_rating, feedback_count) VALUES
(7, 10, 2, '2024-02-01', 25, 222.50, 15, 4.80, 3),
(8, 10, 2, '2024-02-01', 30, 384.00, 12, 4.90, 2),
(5, 9, 1, '2024-02-01', 45, 270.00, 20, 4.50, 5),
(1, 7, 1, '2024-02-01', 60, 348.00, 35, 4.70, 4),
(7, 10, 2, '2024-02-02', 28, 249.20, 18, 4.85, 4),
(10, 11, 2, '2024-02-02', 35, 273.00, 22, 4.60, 3);
