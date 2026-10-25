FROM openjdk:11-jre-slim
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 直接写 jar 包的名字，因为它在云端跟 Dockerfile 在同一级目录
COPY pj-gateway.jar /app.jar

EXPOSE 10010
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "/app.jar"]