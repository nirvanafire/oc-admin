#!/bin/bash

# OC Admin Docker 部署脚本

set -e

echo "=================================="
echo "OC Admin Docker 部署脚本"
echo "=================================="

# 创建必要的目录
echo "[1/5] 创建数据目录..."
mkdir -p data/mysql data/redis
mkdir -p logs/mysql logs/redis logs/app
mkdir -p config/mysql config/redis

# 设置目录权限
echo "[2/5] 设置目录权限..."
chmod -R 755 data logs config

# 检查 .env 文件
echo "[3/5] 检查环境变量配置..."
if [ ! -f .env ]; then
    if [ -f .env.example ]; then
        cp .env.example .env
        echo "已创建 .env 文件，请根据需要修改配置"
    else
        echo "警告: 未找到 .env 或 .env.example 文件"
    fi
else
    echo ".env 文件已存在"
fi

# 构建应用镜像
echo "[4/5] 构建应用镜像..."
if [ !f target/oc-admin-*.jar ]; then
    echo "开始构建 Maven 项目..."
    mvn clean package -DskipTests
fi

docker-compose build app

# 启动服务
echo "[5/5] 启动服务..."
docker-compose up -d

echo ""
echo "=================================="
echo "部署完成！"
echo "=================================="
echo "服务访问地址:"
echo "- 后端 API: http://localhost:8080"
echo "- MySQL: localhost:3306"
echo "- Redis: localhost:6379"
echo ""
echo "查看日志:"
echo "- 应用日志: docker-compose logs -f app"
echo "- MySQL日志: docker-compose logs -f mysql"
echo "- Redis日志: docker-compose logs -f redis"
echo ""
echo "数据目录:"
echo "- MySQL数据: ./data/mysql"
echo "- Redis数据: ./data/redis"
echo ""
echo "日志目录:"
echo "- 应用日志: ./logs/app"
echo "- MySQL日志: ./logs/mysql"