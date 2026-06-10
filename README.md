# HabitLink · 习惯打卡管理系统

> 一套「Web 管理端 + 多端小程序 + Spring Boot 后端」三位一体的习惯养成与打卡追踪系统。后端在通用脚手架（universal-starter）之上落地了习惯、打卡、统计等业务，并集成 RBAC 权限、字典、文件、通知、监控等完整的中后台能力。

<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-17-orange" />
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-3.5.9-brightgreen" />
  <img alt="Vue" src="https://img.shields.io/badge/Vue-3.5-42b883" />
  <img alt="uni-app" src="https://img.shields.io/badge/uni--app-3.0-2b9939" />
  <img alt="License" src="https://img.shields.io/badge/License-MIT-blue" />
</p>

---

## 目录

- [项目简介](#项目简介)
- [整体架构](#整体架构)
- [技术栈](#技术栈)
- [核心功能](#核心功能)
- [后端设计亮点](#后端设计亮点)
- [目录结构](#目录结构)
- [快速开始](#快速开始)
- [Docker 部署](#docker-部署)
- [配置说明](#配置说明)
- [接口约定](#接口约定)
- [License](#license)

---

## 项目简介

HabitLink 帮助用户创建并坚持个人习惯：在小程序 / App / H5 端完成日常打卡与补打卡，查看个人统计与坚持情况；管理员则通过 Web 后台管理用户、习惯数据、字典、文件、系统通知与运行监控。

整个仓库为单体多模块（monorepo）结构，三个子项目可独立开发、独立部署：

| 模块 | 目录 | 说明 | 主要技术 |
| --- | --- | --- | --- |
| 后端服务 | `server/` | 提供全部业务与管理 API | Spring Boot 3 + MyBatis-Plus + Sa-Token + Redis + MySQL |
| Web 管理端 | `web/` | 面向管理员的中后台 | Vue 3 + TypeScript + Vite + Ant Design Vue |
| 多端小程序 | `mp/` | 面向终端用户的打卡应用 | uni-app（Vue 3）+ wot-design-uni + alova |

---

## 整体架构

```text
┌─────────────────────┐      ┌──────────────────────────┐
│   Web 管理端 (web)    │      │   多端小程序 / App (mp)     │
│  Vue3 + Ant Design   │      │  uni-app + wot-design-uni │
└──────────┬───────────┘      └─────────────┬────────────┘
           │  HTTP / WebSocket               │  HTTP
           └──────────────┬──────────────────┘
                          ▼
           ┌──────────────────────────────┐
           │     后端服务 (server)          │
           │  Spring Boot 3 · Java 17      │
           │  Sa-Token 鉴权 · MyBatis-Plus  │
           │  AOP 日志 · WebSocket 通知     │
           └───────┬───────────────┬───────┘
                   ▼               ▼
              ┌────────┐      ┌─────────┐
              │ MySQL  │      │  Redis  │
              └────────┘      └─────────┘
```

- 后端默认端口 `8070`，统一上下文路径 `/api`。
- 鉴权基于 Sa-Token，Token 以 `Bearer` 前缀通过请求头 `Authorization` 传递，并持久化到 Redis。
- 系统通知通过 WebSocket 向 Web 端实时推送。

---

## 技术栈

### 后端 `server`

| 类别 | 选型 |
| --- | --- |
| 基础框架 | Spring Boot 3.5.9 / Java 17 |
| 权限认证 | Sa-Token 1.37.0（整合 Redis Jackson 持久化）|
| 持久层 | MyBatis-Plus 3.5.7 + MyBatis-Plus Generator + Druid 1.2.20 连接池 |
| 数据库 | MySQL 8 |
| 缓存 | Redis（Spring Data Redis / Lettuce）|
| 实时通信 | Spring WebSocket |
| 工具库 | Hutool 5.8.43、Commons-Lang3、ModelMapper 3.2.6、OkHttp 4.12.0、Freemarker（代码生成）|
| 其他 | Spring AOP（操作日志）、`@EnableAsync` 异步、`@EnableScheduling` 定时任务 |

### Web 管理端 `web`

Vue 3.5 + TypeScript + Vite 7 + Ant Design Vue 4.2 + Pinia 3 + Vue Router 4 + ECharts 6 + Axios + UnoCSS（含图标预设）+ `vite-plugin-pages` 文件路由 + `unplugin-auto-import` / `unplugin-vue-components` 自动引入。

### 多端小程序 `mp`

uni-app 3.0（Vue 3 + TypeScript）+ wot-design-uni 组件库 + alova 请求库（含 uniapp 适配器与 mock）+ ECharts（uni-echarts）+ Pinia + vue-i18n + UnoCSS。支持微信 / 支付宝 / 百度 / 抖音 / QQ 等小程序，以及 H5、App（Android/iOS/鸿蒙）多端构建。

---

## 核心功能

### 习惯业务（C 端 / 管理端通用）

- **习惯管理**：创建、编辑、删除（逻辑删除）、暂停、恢复；支持分类、图标、主题色、开始/结束日期、排序、提醒时间等属性。
- **执行规则**：支持每日（`DAILY`）与按周（`WEEKLY`）两种调度规则。
- **打卡**：普通打卡、补打卡（可配置是否允许补打卡及补打卡天数上限），记录打卡来源（小程序 / Web / 系统）。
- **统计**：今日总览、单个习惯的统计详情，配合 ECharts 进行可视化。

### 系统管理（仅管理端）

- **用户管理**：注册、登录、登出、个人资料、修改密码；管理员可分页查询、更新用户、重置密码。
- **角色权限**：基于 Sa-Token 的 RBAC，角色的增删改查、成员查看、用户角色分配。
- **字典管理**：字典类型与字典数据的维护，前端提供字典下拉与字典标签组件。
- **文件管理**：单文件上传、列表/分页、下载、删除、批量删除，支持多种存储后端。
- **系统通知**：管理员发布公告/站内信，用户分页查看、标记已读、未读数量；WebSocket 实时推送。
- **操作日志**：基于 AOP 自动记录，异步落库。
- **系统监控**：运行环境监控信息；自研接口列表（endpoints）与缓存刷新。
- **系统配置**：用户级 / 系统级配置项的读取与更新。

---

## 后端设计亮点

项目刻意以多种经典设计模式组织业务，结构清晰、易扩展：

- **责任链模式（Chain of Responsibility）**
  - 注册校验链 `chain/register`：用户名 → 手机号 → 邮箱 → 密码 逐级校验。
  - 文件校验链 `chain/file`：文件类型、文件大小等校验器串联。
- **策略 + 工厂模式（Strategy + Factory）**
  - 登录策略 `strategy/login`：`LoginStrategyFactory` 按类型分发，已内置密码登录，便于扩展验证码/第三方登录。
  - 存储策略 `strategy/storage`：`StorageStrategyFactory` 统一管理本地、阿里云 OSS、七牛云三种存储实现。
- **观察者模式（Observer）**
  - 通知发布 `observer/notice`：发布通知时同时触发 WebSocket、邮件、短信等多个观察者。
- **AOP 切面**
  - `@Log` 注解 + `LogAspect` + `AsyncLogService` 实现操作日志的无侵入异步记录。
- **自研轻量接口文档**
  - 通过 `@ApiDoc` / `@ApiDocProperty` 注解 + `/system/endpoints` 汇总接口元信息。
- **代码生成器**
  - 基于 MyBatis-Plus Generator + Freemarker（见 `server` 测试目录 `CodeGenerator`），快速生成 entity / mapper / service / controller 等样板代码。

> 统一返回结构 `ApiResult`、全局异常处理 `GlobalExceptionHandler`、雪花/自定义 ID 生成 `IdGenerator` 等基础设施也已就绪；数据库表统一以 `hl_` 前缀命名，并启用逻辑删除与自动填充（创建/更新时间）。

---

## 目录结构

```text
HabitLink-main
├── server/                         # 后端服务 (Spring Boot)
│   └── src/main/java/org/xcyms
│       ├── ApiApplication.java     # 启动类
│       ├── config/                 # 配置（WebSocket、Redis、CORS、Sa-Token、线程池…）
│       ├── controller/             # 接口层（habit / check-in / user / role …）
│       ├── service/                # 业务层
│       ├── mapper/                 # MyBatis-Plus Mapper
│       ├── entity/                 # 实体与 DTO
│       ├── chain/                  # 责任链（注册校验、文件校验）
│       ├── strategy/               # 策略 + 工厂（登录、存储）
│       ├── observer/               # 观察者（通知推送）
│       ├── aspect/                 # AOP 切面（操作日志）
│       ├── common/                 # 通用：ApiResult、枚举、注解、常量
│       ├── task/                   # 定时任务
│       └── utils/                  # 工具类
├── web/                            # Web 管理端 (Vue 3)
│   └── src
│       ├── api/                    # 接口请求封装
│       ├── pages/                  # 页面（按文件路由）
│       ├── components/             # 通用组件
│       ├── store/                  # Pinia 状态
│       └── router/                 # 路由与菜单
├── mp/                             # 多端小程序 (uni-app)
│   └── src
│       ├── pages/                  # 页面（index / habit / records / stats / me / login）
│       ├── api/  components/  store/  composables/
├── docker-compose.yml              # 一键编排：MySQL + Redis + 后端 + Nginx
└── LICENSE                         # MIT
```

---

## 快速开始

### 环境要求

- JDK 17、Maven 3.8+
- MySQL 8、Redis
- Node.js ≥ 20.19（mp 模块要求 ≥ 20.19 / 22.12 / 24）
- 包管理：web 使用 npm，mp 使用 pnpm 9

### 1. 启动后端

```bash
cd server

# 1) 准备数据库：创建库 habit_link，并导入初始化 SQL（若有）
# 2) 按需修改 src/main/resources/application-dev.yml 中的 MySQL / Redis 连接

# 默认激活 dev 环境运行
mvn spring-boot:run
# 或打包后运行
mvn clean package -Pdev
java -jar target/universal-starter-server-1.0.0.jar
```

后端启动后访问基地址为 `http://localhost:8070/api`。

### 2. 启动 Web 管理端

```bash
cd web
npm install
npm run dev          # 开发模式
npm run build        # 生产构建
```

### 3. 启动小程序 / App

```bash
cd mp
pnpm install
pnpm dev:mp-weixin   # 微信小程序（用微信开发者工具打开 dist 目录）
pnpm dev:h5          # H5
pnpm dev:app         # App
# 构建：pnpm build:mp-weixin / build:h5 / build:app ...
```

---

## Docker 部署

根目录提供 `docker-compose.yml`，可一键拉起 MySQL、Redis、后端服务与 Nginx（托管 Web 静态资源）：

```bash
docker compose up -d
```

| 服务 | 容器 | 端口 |
| --- | --- | --- |
| MySQL 8 | universal-mysql | 3306 |
| Redis | universal-redis | 6379 |
| 后端 | universal-server | 8070 |
| Nginx + Web | universal-web | 80 / 443 |

> 注意：编排文件中数据库名、密码等为示例值，生产环境请通过环境变量或私有配置覆盖；`web` 需先 `build` 生成静态资源并挂载，`nginx/conf.d`、`ssl`、`uploads` 等目录请按需准备。

---

## 配置说明

后端关键配置位于 `server/src/main/resources/application-dev.yml`：

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `server.port` | `8070` | 服务端口 |
| `server.servlet.context-path` | `/api` | 统一前缀 |
| 数据源 url | `jdbc:mysql://localhost:3306/habit_link` | MySQL 连接 |
| Redis host/port | `127.0.0.1:6379` | 缓存与 Token 持久化 |
| `sa-token.token-name` | `Authorization` | Token 请求头名 |
| `sa-token.token-prefix` | `Bearer` | Token 前缀 |
| `sa-token.timeout` | `2592000` | Token 有效期（秒，约 30 天）|
| 文件上传 | `max-file-size: 100MB` | 上传大小上限 |

环境通过 Maven Profile（`dev` / `prod` / `test`）切换，默认激活 `dev`。

---

## 接口约定

- **基地址**：`http://<host>:8070/api`
- **鉴权**：登录后获取 Token，后续请求在请求头携带 `Authorization: Bearer <token>`。
- **统一返回**：所有接口返回 `ApiResult` 结构（业务码、消息、数据体），异常由全局处理器统一封装。
- **接口清单**：可调用 `GET /api/system/endpoints` 查看自动汇总的接口元信息。

部分常用接口示例：

| 模块 | 方法 | 路径 | 说明 |
| --- | --- | --- | --- |
| 用户 | POST | `/user/register` | 用户注册 |
| 用户 | POST | `/user/login` | 用户登录 |
| 习惯 | POST | `/habit/create` | 创建习惯 |
| 习惯 | GET | `/habit/list` | 我的习惯列表 |
| 打卡 | POST | `/check-in/submit` | 提交打卡 |
| 打卡 | POST | `/check-in/makeup` | 提交补打卡 |
| 统计 | GET | `/habit-stats/today-overview` | 今日总览 |
| 通知 | GET | `/notice/unread-count` | 未读通知数 |

---

## License

本项目基于 [MIT License](./LICENSE) 开源，版权所有 © 2026 lewisliu。

---

> 文档由阅读源码后整理生成，若实际依赖版本、初始化 SQL、Nginx 配置等与本文档不一致，请以仓库代码为准。
