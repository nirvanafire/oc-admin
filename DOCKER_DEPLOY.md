# Docker 部署指南

## 目录结构

```
oc-admin/
├── docker-compose.yml      # Docker Compose 配置文件
├── Dockerfile              # 应用镜像构建文件
├── .env.example            # 环境变量示例文件
├── deploy.sh               # 一键部署脚本
├── config/                 # 配置文件目录
│   ├── mysql/
│   │   └── my.cnf         # MySQL 配置文件
│   └── redis/
│       └── redis.conf     # Redis 配置文件
├── data/                   # 数据持久化目录（自动创建）
│   ├── mysql/             # MySQL 数据
│   └── redis/             # Redis 数据
├── logs/                   # 日志目录（自动创建）
│   ├── mysql/             # MySQL 日志
│   ├── redis/             # Redis 日志
│   └── app/               # 应用日志
└── target/                # Maven 构建输出目录
    └── oc-admin-*.jar     # 应用 jar 包
```

## 快速开始

### 方式一：使用部署脚本（推荐）

```bash
# 1. 给脚本执行权限
chmod +x deploy.sh

# 2. 运行部署脚本
./deploy.sh
```

### 方式二：手动部署

```bash
# 1. 创建必要目录
mkdir -p data/mysql data/redis logs/mysql logs/redis logs/app

# 2. 复制环境变量配置
cp .env.example .env
# 编辑 .env 文件，修改相关配置

# 3. 构建应用
mvn clean package -DskipTests

# 4. 启动服务
docker-compose up -d
```

## 常用命令

```bash
# 启动所有服务
docker-compose up -d

# 停止所有服务
docker-compose down

# 停止并删除数据卷（谨慎使用）
docker-compose down -v

# 查看所有服务状态
docker-compose ps

# 查看应用日志
docker-compose logs -f app

# 查看 MySQL 日志
docker-compose logs -f mysql

# 查看 Redis 日志
docker-compose logs -f redis

# 重启应用服务
docker-compose restart app

# 进入 MySQL 容器
 docker-compose exec mysql mysql -u root -p

# 进入 Redis 容器
docker-compose exec redis redis-cli

# 进入应用容器
docker-compose exec app sh
```

## 配置说明

### 环境变量 (.env)

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| MYSQL_ROOT_PASSWORD | root123 | MySQL root 密码 |
| MYSQL_USER | ocadmin | MySQL 应用用户名 |
| MYSQL_PASSWORD | ocadmin123 | MySQL 应用密码 |
| JWT_SECRET | ... | JWT 签名密钥 |
| JWT_EXPIRATION | 7200000 | JWT 过期时间（毫秒） |

### 端口映射

| 服务 | 宿主机端口 | 容器端口 | 说明 |
|------|------------|----------|------|
| App | 8080 | 8080 | 后端 API |
| MySQL | 3306 | 3306 | 数据库 |
| Redis | 6379 | 6379 | 缓存 |

### 挂载卷

| 服务 | 宿主机路径 | 容器路径 | 说明 |
|------|------------|----------|------|
| MySQL | ./data/mysql | /var/lib/mysql | 数据文件 |
| MySQL | ./config/mysql/my.cnf | /etc/mysql/conf.d/my.cnf | 配置文件 |
| MySQL | ./logs/mysql | /var/log/mysql | 日志文件 |
| Redis | ./data/redis | /data | 数据文件 |
| Redis | ./config/redis/redis.conf | /usr/local/etc/redis/redis.conf | 配置文件 |
| App | ./logs/app | /app/logs | 应用日志 |

## 数据备份与恢复

### MySQL 备份

```bash
# 备份数据库
docker-compose exec mysql mysqldump -u root -p oc_admin > backup_$(date +%Y%m%d_%H%M%S).sql

# 仅备份结构
docker-compose exec mysql mysqldump -u root -p --no-data oc_admin > backup_schema.sql
```

### MySQL 恢复

```bash
# 恢复数据库
docker-compose exec -T mysql mysql -u root -p oc_admin < backup.sql
```

### Redis 备份

Redis 数据会自动持久化到 `./data/redis` 目录。

```bash
# 手动触发 RDB 备份
docker-compose exec redis redis-cli SAVE

# 备份 AOF 文件
cp data/redis/appendonly.aof backup/appendonly.aof.$(date +%Y%m%d)
```

## 健康检查

所有服务都配置了健康检查：

- **MySQL**: 使用 `mysqladmin ping` 检查
- **Redis**: 使用 `redis-cli ping` 检查
- **App**: 自动检查依赖服务健康状态

应用服务会等待 MySQL 和 Redis 健康后才启动。

## 故障排查

### 应用无法连接 MySQL

```bash
# 检查 MySQL 是否运行
docker-compose ps mysql

# 检查 MySQL 日志
docker-compose logs mysql

# 检查网络连接
docker-compose exec app ping mysql
```

### 端口被占用

```bash
# 检查端口占用
lsof -i :3306
lsof -i :6379
lsof -i :8080

# 修改 .env 中的端口配置
```

### 权限问题

```bash
# 修复目录权限
sudo chown -R $USER:$USER data logs
chmod -R 755 data logs
```

## 生产环境建议

1. **修改默认密码**: 务必修改所有默认密码
2. **使用 HTTPS**: 配置反向代理（Nginx/Traefik）并启用 HTTPS
3. **限制访问**: 使用防火墙限制端口访问
4. **定期备份**: 设置定时任务备份数据库
5. **监控告警**: 集成 Prometheus/Grafana 监控
6. **日志轮转**: 配置日志轮转防止磁盘占满