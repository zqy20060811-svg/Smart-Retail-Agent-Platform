# MXBC 蜜雪冰城点单系统

一个面向门店点单场景的全栈示例项目，提供管理端、用户端和后端服务。项目在开源外卖系统基础上进行二次开发，补充了蜜雪冰城风格的菜单与点单流程，并集成了管理端智能小助手。

> 本项目用于学习、演示和二次开发。仓库中的数据和支付配置仅适合本地调试，不代表完整的生产支付方案。

![管理端预览](./assets/00.png)

## 功能概览

### 管理端

- 工作台：营业数据、订单概览和趋势图表
- 菜品、套餐、分类和门店信息管理
- 订单查询、接单、派送、完成和取消
- 员工账号与权限管理
- 数据报表导出
- WebSocket 新订单提醒
- 智能小助手：流式对话、会话历史，以及员工和营业数据查询

### 用户端

- 微信小程序和 uni-app 两种入口
- 菜品、套餐浏览与分类筛选
- 购物车、地址簿和订单提交
- 订单状态查询与历史订单
- 微信登录接口预留

### 后端服务

- JWT 管理端/用户端鉴权
- MyBatis 数据访问与分页查询
- Redis 缓存和订单相关状态管理
- MongoDB 保存智能小助手的消息记忆
- RustFS（S3 兼容接口）保存图片等对象资源
- Spring Task 定时任务、WebSocket 实时推送
- OpenAI 兼容 API + LangChain4j 智能小助手

## 技术栈

| 模块 | 主要技术 |
| --- | --- |
| 后端 | Java 17、Spring Boot 3.2、Spring MVC、MyBatis、Maven |
| 数据与基础设施 | MySQL 8、Redis 6+、MongoDB 4.4+、RustFS/MinIO SDK |
| 管理端 | Vue 2.6、TypeScript 3.6、Vue CLI 3、Element UI、ECharts |
| 用户端 | uni-app、微信小程序、HBuilder X |
| 智能能力 | LangChain4j、OpenAI 兼容 Chat/Streaming API |

## 仓库结构

```text
.
├── mxbc/                    # Spring Boot Maven 多模块后端
│   ├── sky-common/          # 公共配置、工具和基础组件
│   ├── sky-pojo/            # DTO、VO、Entity
│   └── sky-server/          # 启动模块、Controller、Service、Mapper
├── mxbc-frontend/           # Vue2 + TypeScript 管理端
├── uniapp-hbuilder/         # uni-app 用户端源码
├── mp-weixin/               # 微信开发者工具可直接导入的工程
├── assets/                  # README 预览图
└── sky_take_out.sql         # MySQL 初始化脚本
```

## 环境要求

- JDK 17
- Maven 3.8+
- MySQL 8.0+
- Redis 6+
- MongoDB 4.4+（智能小助手会话记忆）
- Node.js 16.x 和 Yarn 1.x（管理端为 Vue2/旧版 Vue CLI 项目）
- 微信开发者工具（运行 `mp-weixin` 时需要）
- HBuilder X（运行 `uniapp-hbuilder` 时需要）

## 快速开始

### 1. 初始化 MySQL

创建数据库后导入根目录脚本：

```sql
source sky_take_out.sql;
```

脚本默认使用数据库名 `sky_take_out`。如需使用其他名称，请同步修改后端配置。

### 2. 配置并启动后端

后端默认端口为 `8080`，开发环境会加载 `mxbc/sky-server/src/main/resources/env-config.yml`。建议通过操作系统环境变量覆盖其中的默认值，不要把真实凭据提交到 Git。

至少需要准备以下服务：

| 环境变量 | 用途 |
| --- | --- |
| `SKY_MYSQL_HOST`、`SKY_MYSQL_PORT` | MySQL 地址和端口 |
| `SKY_MYSQL_DATABASE`、`SKY_MYSQL_USERNAME`、`SKY_MYSQL_PASSWORD` | 数据库连接 |
| `SKY_REDIS_HOST`、`SKY_REDIS_PORT`、`SKY_REDIS_PASSWORD` | Redis 连接 |
| `SKY_MONGODB_URI` | 智能小助手会话记忆 |
| `SKY_RUSTFS_ENDPOINT`、`SKY_RUSTFS_ACCESS_KEY_ID`、`SKY_RUSTFS_ACCESS_KEY_SECRET` | 对象存储连接 |
| `SKY_RUSTFS_BUCKET_NAME`、`SKY_RUSTFS_PUBLIC_BASE_URL` | Bucket 和图片访问地址 |
| `SKY_AI_BASE_URL`、`SKY_AI_API_KEY`、`SKY_AI_MODEL_NAME` | OpenAI 兼容模型服务 |
| `SKY_WECHAT_*` | 微信登录/支付等能力（按需配置） |

RustFS 需要提前创建对应 Bucket，并允许应用读取对象。`SKY_RUSTFS_PUBLIC_BASE_URL` 应填写浏览器可以访问的域名或反向代理地址。

在 IDE 中运行 `com.sky.SkyApplication`，或使用命令行：

```bash
cd mxbc
mvn -f sky-server/pom.xml spring-boot:run
```

后端启动后，可按项目实际配置访问接口文档（通常为 `/doc.html` 或 `/swagger-ui/index.html`）。

### 3. 启动管理端

管理端默认通过 `/api` 代理后端，并使用 `ws://localhost:8080/ws/` 接收订单通知。先确认 `mxbc-frontend/.env.development` 中的地址与后端一致，再执行：

```bash
cd mxbc-frontend
yarn install
yarn serve
```

生产构建：

```bash
yarn build
```

开发服务器启动后，按终端显示的地址打开管理端登录页。

### 4. 运行微信小程序

1. 安装微信开发者工具并登录自己的账号。
2. 用开发者工具导入 `mp-weixin` 目录。
3. 将项目 AppID 替换为自己的 AppID，并按需修改后端接口地址。
4. 本地调试时确认后端已启动；真机或体验版需要在微信公众平台配置合法域名。

### 5. 运行 uni-app

1. 使用 HBuilder X 打开 `uniapp-hbuilder`。
2. 在 `manifest.json` 配置小程序 AppID。
3. 配置微信开发者工具路径。
4. 选择“运行到微信开发者工具”进行调试。

## 智能小助手

管理端菜单中的“小助手”对应后端 `/admin/chat` 接口，主要配置项为：

- `SKY_AI_BASE_URL`：OpenAI 兼容服务地址
- `SKY_AI_API_KEY`：服务密钥
- `SKY_AI_MODEL_NAME`：模型名称
- `SKY_MONGODB_URI`：会话消息记忆数据库

会话列表保存在 MySQL，消息记忆保存在 MongoDB。小助手内置员工信息和营业数据查询工具；如果没有配置模型服务，管理端其他点单和运营功能不受影响。旧版 `/admin/ai-service` 科大讯飞接口仍保留为兼容入口，需要单独设置 `SKY_LEGACY_XFYUN_*` 配置。

## 常见问题

### 前端安装或构建时报 TypeScript 类型错误

该项目使用较旧的 Vue CLI、TypeScript 和依赖版本，建议使用 Node.js 16.x 与 Yarn 1.x，并清理依赖后重装：

```powershell
cd mxbc-frontend
Remove-Item -Recurse -Force node_modules
yarn install
```

### 后端无法连接数据库

检查 MySQL 服务是否启动，以及 `SKY_MYSQL_*`、`SKY_REDIS_*`、`SKY_MONGODB_URI` 是否指向可访问的实例。首次运行时确认已经导入 `sky_take_out.sql`。

### 图片上传后无法显示

检查 RustFS Endpoint、Bucket、访问密钥和 `SKY_RUSTFS_PUBLIC_BASE_URL`。公网地址应能从浏览器直接访问对象；如果使用反向代理，请将该变量设置为代理后的地址。

### 智能小助手没有回复

确认 `SKY_AI_BASE_URL`、`SKY_AI_API_KEY`、`SKY_AI_MODEL_NAME` 已设置，模型服务网络可达，并检查后端日志。流式回复还需要前端和代理支持长连接。

## 安全说明

- 请立即替换开发配置中的数据库、对象存储、微信和模型服务凭据。
- 不要提交真实密钥、手机号、身份证号、支付证书或生产环境地址。
- 生产环境应更换 JWT 密钥、默认账号密码，并限制数据库、Redis、MongoDB 和对象存储的网络访问。
- 微信支付、退款和回调流程需要使用正式商户配置并完成完整的验签、幂等和风控校验；本仓库仅提供演示接入。

## License

本项目采用 [MIT License](./LICENSE)。
