# 构建阶段
FROM swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/library/maven:3.8.6-openjdk-8-slim AS build
WORKDIR /app

# 先复制 pom.xml 并下载依赖（利用 Docker 层缓存，pom.xml 不变则不会重新下载）
COPY pom.xml .
RUN mvn dependency:resolve dependency:resolve-plugins -B

# 再复制源码并打包
COPY src ./src
RUN mvn clean package -DskipTests

# 运行阶段
FROM swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/library/eclipse-temurin:8-jdk-focal

# 设置时区为 Asia/Shanghai
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 安装 mysql-client (包含 mysqldump)
RUN apt-get update && \
    apt-get install -y --no-install-recommends default-mysql-client nfs-common && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# 复制构建好的 jar
COPY --from=build /app/target/db-backup-server-1.0.0.jar app.jar

# 创建备份临时目录
RUN mkdir -p /app/backup-temp

EXPOSE 8080

ENTRYPOINT ["java", "-Duser.timezone=Asia/Shanghai", "-jar", "app.jar"]
