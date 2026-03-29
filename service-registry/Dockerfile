FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/service-registry-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=75.0","-jar","app.jar"]