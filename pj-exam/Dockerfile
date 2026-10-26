FROM eclipse-temurin:11-jre
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

COPY target/pj-exam.jar /app.jar

EXPOSE 10010
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "/app.jar"]