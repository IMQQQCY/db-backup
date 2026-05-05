# DB Backup Server — MySQL 数据库自动备份服务

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Vue-3.x-blue" alt="Vue 3">
  <img src="https://img.shields.io/badge/Java-8-orange" alt="Java 8">
  <img src="https://img.shields.io/badge/License-MIT-yellow" alt="License">
  <img src="https://img.shields.io/badge/Docker-Ready-blue" alt="Docker">
</p>

一个轻量级、自托管的 MySQL 数据库自动备份服务，提供现代化 Web 管理界面。支持定时备份、多种存储方式（本地/NFS）、邮件告警通知和灵活的保留策略。

## 功能特性

- **多数据源管理** — 配置和管理多个 MySQL 数据库连接，支持连接测试
- **灵活的备份类型** — 支持全量备份和部分表备份（表选择支持分页、搜索、大小展示）
- **备份内容可选** — 结构+数据 或 仅结构（DDL）备份
- **定时备份调度** — 基于 Cron 表达式的定时任务，提供表达式测试和常用模板快捷选择
- **多存储后端** — 本地存储 或 NFS 远程存储（程序自动挂载/卸载）
- **保留策略** — 可配置保留天数，自动清理过期备份文件
- **邮件通知** — 备份成功/失败通过 SMTP 邮件通知
- **现代化 Web UI** — Vue 3 + Element Plus，渐变配色设计，响应式布局
- **Docker 一键部署** — Docker Compose 一条命令启动
- **时区支持** — 容器内正确支持 Asia/Shanghai 时区

## 系统架构

```
┌─────────────────┐     ┌──────────────────────────────┐
│   Vue 3 前端    │────▶│  Spring Boot 后端服务 (8080)  │
│  (Element Plus) │     │                              │
└─────────────────┘     │  ┌────────────────────────┐  │
                        │  │  Quartz 定时调度器      │  │
                        │  │  (Cron 表达式驱动)     │  │
                        │  └────────────────────────┘  │
                        │  ┌────────────────────────┐  │
                        │  │  mysqldump 备份执行器  │  │
                        │  │  (--single-transaction)│  │
                        │  └────────────────────────┘  │
                        │  ┌────────────────────────┐  │
                        │  │  存储层 (本地 / NFS)    │  │
                        │  └────────────────────────┘  │
                        └──────────────────────────────┘
```

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 8、Spring Boot 2.7.18、MyBatis-Plus 3.5.5 |
| 调度 | Quartz |
| 数据库 | MySQL（元数据存储） |
| 备份工具 | mysqldump + Gzip 压缩 |
| 前端 | Vue 3、Vite 5、Element Plus |
| 部署 | Docker、Docker Compose |

## 快速开始

### 环境要求

- Docker & Docker Compose
- 一个 MySQL 实例（用于存储备份服务自身的元数据）

### 1. 克隆仓库

```bash
git clone https://github.com/your-username/db-backup-server.git
cd db-backup-server
```

### 2. 配置环境变量

编辑 `docker-compose.yml`，设置元数据库连接信息：

```yaml
environment:
  - SPRING_DATASOURCE_URL=jdbc:mysql://你的MySQL地址:3306/db_backup?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
  - SPRING_DATASOURCE_USERNAME=root
  - SPRING_DATASOURCE_PASSWORD=你的密码
```

### 3. 初始化数据库

在你的 MySQL 实例上执行 `db-backup-server/src/main/resources/schema.sql` 创建所需表。

### 4. 构建并启动

```bash
docker-compose up -d --build
```

### 5. 访问管理界面

打开浏览器访问：`http://localhost:8080`

## 页面功能

| 页面 | 功能说明 |
|------|---------|
| 概览 | 统计卡片（数据源数、任务数、历史数、成功数）+ 最近备份记录 |
| 数据源管理 | 新增/编辑/删除/测试 MySQL 连接 |
| 备份任务 | 创建定时任务，支持 Cron 测试、表选择（分页+搜索+排序）、备份内容选择 |
| 备份历史 | 查看所有备份执行记录，含状态、文件路径、大小、耗时 |
| NFS 配置 | 管理 NFS 挂载点，支持在线挂载/卸载 |
| 邮件配置 | SMTP 配置，支持测试邮件发送 |

## 配置说明

### 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `SPRING_DATASOURCE_URL` | 元数据库 JDBC 连接地址 | `jdbc:mysql://localhost:3306/db_backup` |
| `SPRING_DATASOURCE_USERNAME` | 数据库用户名 | `root` |
| `SPRING_DATASOURCE_PASSWORD` | 数据库密码 | `root` |
| `BACKUP_TEMP_DIR` | 备份文件临时存储目录 | `./backup-temp` |

### 备份选项说明

| 选项 | 说明 |
|------|------|
| 备份类型 | `FULL`（整库）或 `PARTIAL`（选择部分表） |
| 备份内容 | `STRUCTURE_DATA`（结构+数据）或 `STRUCTURE`（仅DDL，不含数据） |
| 存储类型 | `LOCAL`（本地文件系统）或 `NFS`（远程NFS挂载） |
| Cron 表达式 | 标准 6 位 cron（秒 分 时 日 月 周） |
| 保留天数 | 备份文件保留的天数，过期自动清理 |

### mysqldump 参数

备份执行时使用的 mysqldump 参数：

| 参数 | 作用 |
|------|------|
| `--single-transaction` | InnoDB 一致性快照读，不锁表 |
| `--routines` | 包含存储过程和函数 |
| `--triggers` | 包含触发器 |
| `--hex-blob` | 二进制数据用十六进制导出 |
| `--force` | 遇到错误继续执行（跳过无效视图等） |
| `--no-tablespaces` | 避免 PROCESS 权限问题 |
| `--no-data` | 仅结构备份时使用 |

## 本地开发

### 后端

```bash
cd db-backup-server
mvn clean package -DskipTests
java -jar target/db-backup-server-1.0.0.jar
```

### 前端

```bash
cd db-backup-web
npm install
npm run dev    # 开发服务器 http://localhost:5173
npm run build  # 生产构建
```

## 项目结构

```
db-backup-server/
├── db-backup-server/          # 后端工程 (Spring Boot)
│   ├── src/main/java/com/backup/server/
│   │   ├── controller/        # REST 接口层
│   │   ├── service/           # 业务逻辑层
│   │   ├── entity/            # 数据库实体
│   │   ├── mapper/            # MyBatis-Plus Mapper
│   │   ├── dto/               # 数据传输对象
│   │   ├── enums/             # 枚举类
│   │   ├── config/            # 配置类（CORS、Quartz、MyBatis）
│   │   └── util/              # 工具类（MySqlDumpUtil、NfsMountUtil）
│   ├── src/main/resources/
│   │   ├── application.yml    # 应用配置
│   │   └── schema.sql         # 数据库初始化脚本
│   └── Dockerfile
├── db-backup-web/             # 前端工程 (Vue 3)
│   ├── src/
│   │   ├── views/             # 页面组件
│   │   ├── api/               # API 请求模块
│   │   └── router/            # 路由配置
│   └── package.json
├── docker-compose.yml         # Docker 编排文件
├── README.md                  # English documentation
└── README_CN.md               # 中文文档
```

## 常见问题

**Q: 备份报错 "View references invalid table" 怎么办？**

A: 程序已使用 `--force` 参数，会跳过有问题的视图继续备份其他对象。如果仍有问题，请检查备份用户是否对所有视图有足够权限。

**Q: 如何备份到 NFS？**

A: 在 Web 界面的"NFS 配置"页面添加 NFS 挂载信息，创建备份任务时选择存储类型为 NFS。程序会在备份执行时自动挂载，完成后自动卸载。

**Q: 可以不用 Docker 部署吗？**

A: 可以。直接编译后端 JAR 包，用 Java 8+ 运行即可。需要确保系统 PATH 中有 `mysqldump` 命令。

**Q: 全量备份大库会影响性能吗？**

A: 使用了 `--single-transaction` 参数，基于 InnoDB MVCC 快照读，备份期间不会锁表，对线上业务影响较小。对于超大库（>50GB），建议安排在低峰期执行或考虑从从库备份。

**Q: 定时任务执行时间不准？**

A: 确保 Docker 容器时区设置正确。本项目已在 Dockerfile 中配置 `TZ=Asia/Shanghai` 和 JVM 参数 `-Duser.timezone=Asia/Shanghai`。

## 开源协议

本项目基于 MIT 协议开源。
