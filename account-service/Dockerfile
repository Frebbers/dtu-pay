FROM eclipse-temurin:25 as jre-build
WORKDIR /usr/src
#TODO Fix this! account-service is not using quarkus
COPY target/quarkus-app /usr/src/quarkus-app
CMD java -Xmx64m \
-jar quarkus-app/quarkus-run.jar
