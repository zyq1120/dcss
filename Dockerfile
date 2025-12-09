# ========================================
# 智能文档处理系统 - 多阶段构建
# 阶段1: 构建阶段
# 阶段2: 运行阶段
# ========================================

# ========================================
# 阶段1: 构建应用
# ========================================
FROM maven:3.8.8-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# 复制 pom.xml 并下载依赖（利用Docker缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码并构建
COPY src ./src
RUN mvn clean package -DskipTests -B

# ========================================
# 阶段2: 运行应用
# ========================================
FROM eclipse-temurin:17-jre-alpine

# 安装必要工具
RUN apk add --no-cache curl tzdata

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 创建应用用户（安全性）
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# 设置工作目录
WORKDIR /app

# 从构建阶段复制jar包
COPY --from=builder /build/target/*.jar app.jar

# 创建日志目录和临时目录
RUN mkdir -p /app/logs /app/temp && \
    chown -R appuser:appgroup /app

# 切换到应用用户
USER appuser

# 暴露端口
EXPOSE 8080

# JVM参数优化
ENV JAVA_OPTS="-Xms512m -Xmx1024m \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:+HeapDumpOnOutOfMemoryError \
    -XX:HeapDumpPath=/app/logs/heap-dump.hprof \
    -Djava.security.egd=file:/dev/./urandom \
    -Dfile.encoding=UTF-8 \
    -Djava.io.tmpdir=/app/temp"

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

