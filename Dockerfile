FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

COPY spendwise-backend/pom.xml ./pom.xml
COPY spendwise-backend/src ./src

RUN mvn -B -DskipTests package

FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /app/target/spendwise-backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 9999

ENTRYPOINT ["sh", "-c", "java -jar app.jar"]