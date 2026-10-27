FROM eclipse-temurin:11-jre
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
COPY target/pj-stock.jar /app.jar
ENTRYPOINT ["java", "-Xms128m", "-Xmx128m", "-Xss256k", "-XX:MaxMetaspaceSize=96m", "-XX:MaxDirectMemorySize=64m",  "-jar", "/app.jar"]