FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY . .
RUN mvn -q -Dmaven.test.skip=true -pl services/glucose-processor -am package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/services/glucose-processor/target/glucose-processor-*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=75.0","-jar","/app/app.jar"]


