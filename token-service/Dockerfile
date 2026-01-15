FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY utilities/messaging-utilities/pom.xml utilities/messaging-utilities/pom.xml
COPY utilities/messaging-utilities/src utilities/messaging-utilities/src
RUN mvn -q -f utilities/messaging-utilities/pom.xml install
COPY token-service/pom.xml token-service/pom.xml
COPY token-service/src token-service/src
COPY token-service/features token-service/features
RUN mvn -q -f token-service/pom.xml package dependency:copy-dependencies -DincludeScope=runtime

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/token-service/target/token-service-*.jar /app/token-service.jar
COPY --from=build /workspace/token-service/target/dependency /app/dependency
CMD ["java", "-cp", "token-service.jar:dependency/*", "dtu.token.TokenServiceApplication"]
