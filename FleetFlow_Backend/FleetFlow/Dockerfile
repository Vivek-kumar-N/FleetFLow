# ─────────────────────────────────────────────
# Stage 1 – Build the JAR
# ─────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy dependency descriptor first so Docker can cache the layer.
# Re-downloading dependencies only happens when pom.xml changes.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build the fat JAR, skipping tests for faster image builds.
COPY src ./src
RUN mvn package -DskipTests -B

# ─────────────────────────────────────────────
# Stage 2 – Minimal runtime image
# ─────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy only the executable JAR from the builder stage.
# artifactId=carcaddy, version=0.0.1-SNAPSHOT → carcaddy-0.0.1-SNAPSHOT.jar
COPY --from=builder /app/target/carcaddy-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
