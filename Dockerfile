# ---------- Build stage ----------
FROM eclipse-temurin:21 AS build
WORKDIR /workspace

# Copy Maven wrapper + pom first to leverage Docker layer caching
COPY mvnw ./
COPY .mvn/ .mvn/
COPY pom.xml ./

# Pre-fetch dependencies (better caching)
RUN ./mvnw -q -e -DskipTests dependency:go-offline

# Copy the rest of the source and build
COPY src/ src/
RUN ./mvnw -q -DskipTests package

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /usr/src

# Copy the Quarkus "fast-jar" output created by `mvn package`
COPY --from=build /workspace/target/quarkus-app/ ./quarkus-app/

# Quarkus default HTTP port (optional but conventional)
EXPOSE 8080

CMD ["java", "-Xmx64m", "-jar", "quarkus-app/quarkus-run.jar"]
