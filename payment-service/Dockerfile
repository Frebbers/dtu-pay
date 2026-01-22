FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY utilities/messaging-utilities/pom.xml utilities/messaging-utilities/pom.xml
COPY utilities/messaging-utilities/src utilities/messaging-utilities/src
RUN mvn -q -f utilities/messaging-utilities/pom.xml install
COPY payment-service/pom.xml payment-service/pom.xml
COPY payment-service/src payment-service/src
COPY payment-service/features payment-service/features
RUN mvn -f payment-service/pom.xml package dependency:copy-dependencies -DincludeScope=runtime

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/payment-service/target/payment-service-*.jar /app/payment-service.jar
COPY --from=build /workspace/payment-service/target/dependency /app/dependency
CMD ["java", "-cp", "payment-service.jar:dependency/*", "payment.service.startup.App"]