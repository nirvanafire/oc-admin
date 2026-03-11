# 使用官方 OpenJDK 17 镜像作为基础镜像
FROM eclipse-temurin:17-jdk-alpine

# 设置时区
ENV TZ=Asia/Shanghai
RUN apk add --no-cache tzdata && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone

# 创建应用目录
WORKDIR /app

# 复制 Maven 构建的 jar 文件
COPY target/oc-admin-*.jar app.jar

# 创建日志目录
RUN mkdir -p /app/logs

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=docker", "app.jar"]