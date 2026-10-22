# ============================================================
# Stage 1 — Build
# Official Maven 3.9.6 image with Eclipse Temurin JDK 21
# Matches: pom.xml <java.version>21</java.version> + Spring Boot 3.2.5
#          .mvn/wrapper/maven-wrapper.properties → apache-maven-3.9.6
# Note: mvnw.cmd is Windows-only and cannot run inside a Linux container.
#       The maven:3.9.6-eclipse-temurin-21 image ships Maven pre-installed,
#       so no wrapper script is needed at all.
# ============================================================
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# ------------------------------------------------------------------
# Layer-cache optimisation:
#   Copy only the dependency descriptors first. The RUN below
#   downloads all dependencies into the local repo. This layer is
#   only invalidated when pom.xml or .mvn/ changes — not when
#   source files change.
# ------------------------------------------------------------------
COPY pom.xml .
COPY .mvn/ .mvn/

RUN mvn dependency:go-offline -B --no-transfer-progress

# Copy the full source tree and build the fat JAR
COPY src/ src/

RUN mvn clean package -DskipTests -B --no-transfer-progress

# ============================================================
# Stage 2 — Runtime
# Minimal JRE-only image — no JDK or Maven shipped to production.
# ============================================================
FROM eclipse-temurin:21-jre-jammy AS runtime

# Non-root user for container security
RUN groupadd --system cartnest && \
    useradd  --system --gid cartnest --home /app --shell /usr/sbin/nologin cartnest

WORKDIR /app

# Copy the packaged Spring Boot fat JAR from the build stage.
# Filename = <artifactId>-<version>.jar from pom.xml → backend-0.0.1-SNAPSHOT.jar
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar app.jar

RUN chown cartnest:cartnest app.jar

USER cartnest

# ------------------------------------------------------------------
# PORT binding for Render (and similar platforms):
#   Render injects the PORT environment variable at runtime.
#   Spring Boot's relaxed binding maps SERVER_PORT → server.port,
#   so setting SERVER_PORT here is enough.
#   Override by passing -e PORT=<n> or setting SERVER_PORT on Render.
# ------------------------------------------------------------------
ENV SERVER_PORT=8080
EXPOSE 8080

# exec form → JVM receives SIGTERM directly for graceful shutdown
ENTRYPOINT ["java", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-Dserver.port=${SERVER_PORT}", \
            "-jar", "app.jar"]

