FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/account-service-*.jar /app/account-service.jar
COPY target/dependency /app/dependency
ENV RABBITMQ_HOST=rabbitMq
CMD ["java", "-cp", "account-service.jar:dependency/*", "dtu.pay.accountService.AccountServiceApplication"]
