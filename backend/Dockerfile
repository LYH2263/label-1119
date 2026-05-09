# 构建阶段
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# 拷贝 Maven 配置和 pom.xml
COPY settings.xml .
COPY pom.xml .

# 预下载依赖 (利用 Docker 缓存层)
RUN mvn -s settings.xml dependency:go-offline -B

# 拷贝源码并打包
COPY src ./src
RUN mvn -s settings.xml package -DskipTests

# 运行阶段
FROM eclipse-temurin:17-jre
WORKDIR /app

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 复制 JAR 文件
COPY --from=build /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8000

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]
