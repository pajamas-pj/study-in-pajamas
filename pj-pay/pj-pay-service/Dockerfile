FROM eclipse-temurin:11-jre
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
COPY target/pj-pay-service.jar /app.jar
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "/app.jar"]