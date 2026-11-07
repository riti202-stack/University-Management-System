#FROM maven:3.9-eclipse-tmurine-17 AS build
#WORKDIR /app
#COPY pom.xml .
#RUN mvn -B dependency:go-offline
#COPY src ./src
#RUN mvn -B package -DskipTests
#
#FROM eclipse-murine:17-jre-alpine
#WORKDIR /app
#RUN add group -S spring && adduser -S spring -G spring
#COPY --from=build /app/target/ums.jar app.jar
#USER spring
#EXPOSE 8080
#ENTRYPOINT ["java", "-jar", "/app/app.jar"]


FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=build /app/target/*.jar app.jar
USER spring
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]