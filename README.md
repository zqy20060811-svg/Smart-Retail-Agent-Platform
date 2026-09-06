# 智能零售客服与订单协同平台

面向零售点单业务，构建集 **商品、订单、用户、AI 客服** 于一体的智能业务平台。
将传统订单系统与 **LLM Agent（Dify）** 能力结合：AI 客服通过 Agent Tools 调用订单、商品、优惠知识库，直接回答用户的业务问题。

## 功能与技术点

| 能力 | 实现 |
| --- | --- |
| 核心业务模块 | Spring Boot 2.7 + MyBatis-Plus，用户/商品/套餐/订单 CRUD、分页、条件查询、逻辑删除、公共字段自动填充 |
| 数据存储 | MySQL 8：用户、商品、套餐、订单、订单明细、优惠、AI 会话等表，高频字段建索引（订单号唯一索引、用户+状态联合索引等） |
| 缓存 | Redis 缓存商品热点数据；空值缓存防 **穿透**、SETNX 互斥锁防 **击穿**、随机 TTL 防 **雪崩**；Redis 维护 AI 短期会话上下文 |
| 实时推送 | WebSocket（`/ws/order/{token}`）订单状态变更实时推送用户端 |
| AI Agent | Dify Agent 集成：订单查询 / 商品搜索 / 优惠查询封装为 Agent Tools（Dify 回调端点）；未配置 Dify 时自动降级为基于本地知识库的 mock 回复 |
| 安全 | JWT（用户端/管理端双令牌）、`@RestControllerAdvice` 统一异常、`@Validated` 参数校验、拦截器鉴权、Tools 回调密钥校验 |
| 限流 | 自定义 `@RateLimit` 注解 + AOP + Redis 固定窗口，用于 AI 对话接口 |
| 接口文档 | Knife4j：`http://localhost:8080/doc.html` |

## 项目结构

```text
.
├── backend/                  # Spring Boot + MyBatis-Plus 后端（单模块）
│   ├── pom.xml
│   └── src/main/java/com/retail/
│       ├── RetailApplication.java
│       ├── common/           # Result 封装、全局异常、常量、枚举、ThreadLocal 上下文
│       ├── config/           # MP/Redis/WebMvc/WebSocket/Knife4j 配置、配置属性类
│       ├── security/         # JWT 工具、user/admin/tool 三个拦截器、@RateLimit 限流切面
│       ├── entity/           # 11 个实体（MyBatis-Plus 注解、逻辑删除）
│       ├── mapper/           # BaseMapper 接口
│       ├── service/          # 业务层（含缓存保护、订单事务、AI 会话）
│       ├── ai/               # DifyClient、AgentToolService（订单/商品/优惠工具集）
│       ├── controller/
│       │   ├── user/         # C 端接口 /api/user/**
│       │   ├── admin/        # 管理端接口 /api/admin/**
│       │   └── ai/           # Dify Tools 回调 /api/ai/tools/**
│       └── websocket/        # 订单状态推送
├── user-web/                 # 用户 web 端（Streamlit，端口 8504）：登录/商品下单/订单/AI 客服
│   ├── app.py
│   ├── client.py
│   └── requirements.txt
├── admin-web/                # 商家管理端（Streamlit，端口 8503）：工作台/商品/分类/订单/优惠管理
│   ├── app.py
│   ├── client.py
│   └── requirements.txt
├── sql/init.sql              # 建库建表 + 索引 + 演示数据
└── docker-compose.yml        # 可选：一键 MySQL + Redis
```

## 快速开始

### 1. 准备依赖服务

- JDK 17、Maven 3.8+
- MySQL 8、Redis 6+（本机已有可直接用；或 `docker compose up -d` 一键启动）
- Python 3.10+（用户 web 端）

### 2. 初始化数据库

```sql
source sql/init.sql;   -- 库名 smart_retail，含演示数据
```

默认账号：
- 管理员 `admin / 123456`
- 演示用户 `demo / 123456`

### 3. 启动后端（端口 8080）

```bash
cd backend
mvn spring-boot:run
```

数据库/Redis 默认连接 `localhost:3306`（root/1234）和 `localhost:6379`（db 10），
可通过环境变量覆盖：`retail.mysql.host/password`、`retail.redis.host/port/database` 等（见 `application.yml`）。

接口文档：http://localhost:8080/doc.html

### 4. 启动用户 web 端（Streamlit，端口 8504）

```bash
cd user-web
pip install -r requirements.txt
streamlit run app.py --server.port 8504
```

浏览器打开 http://localhost:8504 ，用 `demo / 123456` 登录，可浏览商品、下单、查订单、和 AI 客服对话。

### 5. 启动商家管理端（Streamlit，端口 8503）

```bash
cd admin-web
pip install -r requirements.txt
streamlit run app.py --server.port 8503
```

浏览器打开 http://localhost:8503 ，用 `admin / 123456` 登录，可查看工作台经营数据、管理商品/分类/优惠活动、接单并流转订单状态（状态变更经 WebSocket 实时推送给用户端）。

## AI 客服与 Dify 接入

**默认降级模式**：未配置 Dify 也能完整体验——AI 客服通过本地知识库真实查询订单/商品/优惠数据生成回复。

**接入 Dify**（`application.yml` 中 `retail.dify`）：

```yaml
retail:
  dify:
    enabled: true
    base-url: https://your-dify-host   # Dify 服务地址
    api-key: app-xxxxxxxxxxxx          # Dify App API-Key
  tool:
    key: your-tool-secret              # Dify 回调 Tools 的共享密钥
```

Agent Tools 端点（在 Dify 中注册为自定义工具，请求头带 `X-Tool-Key`）：

| 端点 | 能力 |
| --- | --- |
| `POST /api/ai/tools/order-query` | 按用户/订单号/状态查订单 |
| `POST /api/ai/tools/product-search` | 商品名模糊搜索 |
| `POST /api/ai/tools/discount-query` | 查询进行中的优惠活动 |

## 主要接口

用户端（登录后请求头带 `token`）：
- `POST /api/user/auth/register|login`，`GET /api/user/auth/me`
- `GET /api/user/categories|products|setmeals`，`GET /api/user/products/{id}`
- `POST /api/user/orders`，`GET /api/user/orders`，`GET /api/user/orders/{id}`，`PUT /api/user/orders/{id}/cancel`
- `POST /api/user/ai/chat`（限流 60s/10 次），`GET /api/user/ai/sessions`

管理端（商家端 Web 使用，admin / 123456）：
- `POST /api/admin/auth/login`
- `GET /api/admin/workspace/stats`（工作台经营数据）
- `GET/POST/PUT/DELETE /api/admin/products/**`
- `GET/POST/PUT/DELETE /api/admin/category/**`
- `GET/POST/PUT/DELETE /api/admin/promotion/**`
- `GET /api/admin/orders/page`，`GET /api/admin/orders/{id}`，`PUT /api/admin/orders/{id}/status/{status}`（触发 WebSocket 推送）

## 待完善（TODO）

- 订单状态机校验、库存扣减（乐观锁防超卖）、支付回调
- Dify SSE 流式回复（当前 blocking）、Tools 调用记录落库（`tool_name`）
- 接口压测与性能报告

## License

MIT
