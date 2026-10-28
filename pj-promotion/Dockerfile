FROM eclipse-temurin:11-jre
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
COPY target/pj-promotion.jar /app.jar
ENTRYPOINT ["java","-server","-Xms256m","-Xmx256m","-Xss512k","-XX:MaxMetaspaceSize=128m","-XX:MaxDirectMemorySize=32m","-XX:+UseSerialGC","-jar", "/app.jar"]