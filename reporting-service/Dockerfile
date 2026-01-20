FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY utilities/messaging-utilities/pom.xml utilities/messaging-utilities/pom.xml
COPY utilities/messaging-utilities/src utilities/messaging-utilities/src
RUN mvn -q -f utilities/messaging-utilities/pom.xml install
COPY reporting-service/pom.xml reporting-service/pom.xml
COPY reporting-service/src reporting-service/src
COPY reporting-service/features reporting-service/features
RUN mvn -q -f reporting-service/pom.xml package dependency:copy-dependencies -DincludeScope=runtime

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/reporting-service/target/reporting-service-*.jar /app/reporting-service.jar
COPY --from=build /workspace/reporting-service/target/dependency /app/dependency
CMD ["java", "-cp", "reporting-service.jar:dependency/*", "dtu.reporting.ReportingServiceApplication"]
