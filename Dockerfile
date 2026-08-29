# 使用轻量级 JDK 基础镜像
FROM openjdk:17-ea-17-jdk-slim

# 设置工作目录
WORKDIR /app

# 设置时区（可选，但推荐）
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 复制 JAR 文件到镜像内
# 注意：需要与 GitHub Actions 构建产物路径保持一致（target/*.jar）
COPY target/*.jar app.jar

# 创建非 root 用户运行应用（提升安全性）
RUN groupadd -r appuser && useradd -r -g appuser appuser
USER appuser

# 暴露应用端口（根据你的 Spring Boot 项目实际端口调整）
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]