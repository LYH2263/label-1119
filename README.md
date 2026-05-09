# 环生鲜电商平台

环生鲜是一个响应国家"三农"政策的生鲜农产品电商平台，通过采优园大数据分析系统收集消费者反馈，帮助农产品供应商改进种植方案，促进农业经济发展。

## 🛠 技术栈

- **Frontend**: HTML5 + CSS3 + JavaScript (原生:前端项目，没有使用框架，只html、css、js来实现)
- **Backend**: Java 17 + Spring Boot 3.2 + MyBatis-Plus (这是后端，后端可以用框架)
- **Database**: MySQL 8.0
- **容器化**: Docker + Docker Compose

## 🚀 启动指南 (How to Run)

### 前置要求

- Docker Desktop 已安装并启动

### 一键启动

1. 进入项目根目录：
   ```bash
   cd label-1119
   ```

2. 执行启动命令：
   ```bash
   docker compose up --build
   ```

3. 等待容器启动完成（首次构建约需 3-5 分钟）

4. 当看到后端日志显示 `Started HuanShengXianApplication` 时，表示启动成功

### 停止服务

```bash
docker compose down
```

### 清除数据并重新启动

```bash
docker compose down -v
docker compose up --build
```

## 🔗 服务地址 (Services)
先清除浏览器缓存/使用无痕模式打开（其他项目错误token缓存可能造成页面报错），项目内的商品图与轮播图已内置到前端静态资源目录，可离线稳定访问。：
| 服务 | 地址 |
|------|------|
| 前端页面 | http://localhost:3000 |
| 后端 API | http://localhost:8000/api |
| Swagger 文档 | http://localhost:8000/api/swagger-ui.html |
| 数据库 | localhost:3306 |

## 🧪 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |
| 供应商 | supplier1 | 123456 |
| 普通用户 | user1 | 123456 |

## 📦 功能模块

### 用户端

- ✅ 用户注册/登录
- ✅ 商品浏览（分类、搜索、筛选）
- ✅ 商品详情
- ✅ 购物车管理
- ✅ 订单管理
- ✅ 收货地址管理
- ✅ 商品评价/反馈

### 数据分析

- ✅ 销售数据概览
- ✅ 热销商品排行
- ✅ 分类销售统计
- ✅ 消费者反馈分析
- ✅ 数据反馈给供应商

## 🏗 项目结构

```
label-1119/
├── docker-compose.yml      # Docker 编排配置
├── .dockerignore           # Docker 忽略文件
├── .gitignore              # Git 忽略文件
├── README.md               # 项目说明
│
├── frontend/               # 前端项目
│   ├── Dockerfile          # 前端容器配置
│   ├── nginx.conf          # Nginx 配置
│   ├── index.html          # 主页面
│   ├── css/
│   │   └── style.css       # 样式文件
│   └── js/
│       └── app.js          # JavaScript 脚本
│
├── backend/                # 后端项目
│   ├── Dockerfile          # 后端容器配置
│   ├── settings.xml        # Maven 镜像配置
│   ├── pom.xml             # Maven 依赖配置
│   └── src/
│       └── main/
│           ├── java/com/huanshengxian/
│           │   ├── HuanShengXianApplication.java
│           │   ├── config/         # 配置类
│           │   ├── controller/     # 控制器
│           │   ├── service/        # 服务层
│           │   ├── mapper/         # 数据访问层
│           │   ├── entity/         # 实体类
│           │   ├── common/         # 公共类
│           │   └── security/       # 安全相关
│           └── resources/
│               └── application.yml # 应用配置
│
└── mysql/
    └── init.sql            # 数据库初始化脚本
```

## 🔧 API 接口

### 认证相关

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/auth/login | POST | 用户登录 |
| /api/auth/register | POST | 用户注册 |
| /api/auth/me | GET | 获取当前用户 |

### 商品相关

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/products | GET | 商品列表(分页) |
| /api/products/hot | GET | 热销商品 |
| /api/products/new | GET | 新品推荐 |
| /api/products/{id} | GET | 商品详情 |

### 分类相关

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/categories/tree | GET | 分类树 |
| /api/categories/top | GET | 一级分类 |

### 购物车相关

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/cart | GET | 获取购物车 |
| /api/cart | POST | 添加到购物车 |
| /api/cart/{productId} | PUT | 更新数量 |
| /api/cart/{productId} | DELETE | 移除商品 |

### 订单相关

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/orders | GET | 订单列表 |
| /api/orders | POST | 创建订单 |
| /api/orders/{id} | GET | 订单详情 |
| /api/orders/{id}/pay | PUT | 支付订单 |
| /api/orders/{id}/cancel | PUT | 取消订单 |

### 数据分析

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/analytics/overview | GET | 销售概览 |
| /api/analytics/hot-products | GET | 热销排行 |
| /api/analytics/category-sales | GET | 分类销售 |
| /api/analytics/feedback-stats | GET | 反馈统计 |

## 🐳 Docker 配置说明

### 端口映射

- 前端: 3000:3000
- 后端: 8000:8000
- MySQL: 3306:3306

### 数据持久化

MySQL 数据通过 Docker Volume 持久化存储

### 网络配置

所有服务通过 `huanshengxian-network` 内部网络通信

## 📝 开发说明

### 数据库连接

- Host: localhost (外部) / db (Docker内部)
- Port: 3306
- Database: huanshengxian
- Username: huanshengxian
- Password: huanshengxian123

### 演示数据

系统启动时会自动初始化以下演示数据：
- 5 个用户（管理员、供应商、普通用户）
- 2 个供应商
- 12 个商品分类
- 12 个商品
- 4 张轮播图
- 3 个演示订单
- 4 条用户反馈

## 🌟 特色功能

1. **响应式布局** - 完美适配 PC 端和移动端
2. **骨架屏加载** - 优化用户体验
3. **数据分析展示** - 实时展示销售数据和用户反馈
4. **三农政策响应** - 通过数据反馈帮助农产品供应商改进

## 📄 许可证

MIT License

---

© 2024 环生鲜电商平台 | 响应"三农"政策，促进农业经济发展
