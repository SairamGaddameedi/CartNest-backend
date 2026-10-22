# ============================================================
# Stage 1 — Build
# Uses Eclipse Temurin JDK 21 (LTS) + Maven 3.9 to match pom.xml
# ============================================================
FROM eclipse-temurin:21-jdk-jammy AS build

# Install Maven 3.9.6 (matches maven-wrapper.properties distributionUrl)
RUN apt-get update && apt-get install -y wget && \
    wget -q https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.tar.gz \
         -O /tmp/maven.tar.gz && \
    tar -xzf /tmp/maven.tar.gz -C /opt && \
    ln -s /opt/apache-maven-3.9.6/bin/mvn /usr/local/bin/mvn && \
    rm /tmp/maven.tar.gz && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# ------------------------------------------------------------------
# Copy dependency descriptors first so Docker can cache the
# dependency-download layer independently of source changes.
# ------------------------------------------------------------------
COPY pom.xml .
COPY .mvn/ .mvn/

# Download all dependencies (offline cache) — re-run only if pom.xml changes
RUN mvn dependency:go-offline -B

# Copy the full source tree
COPY src/ src/

# Build the fat JAR, skipping tests (tests need a live DB / secrets)
RUN mvn package -DskipTests -B

# ============================================================
# Stage 2 — Runtime
# Minimal JRE image; no build tooling shipped to production
# ============================================================
FROM eclipse-temurin:21-jre-jammy AS runtime

# Create a non-root user for security
RUN groupadd --system cartnest && \
    useradd  --system --gid cartnest --home /app --shell /usr/sbin/nologin cartnest

WORKDIR /app

# Copy only the packaged JAR from the build stage
# Spring Boot Maven plugin always names it: <artifactId>-<version>.jar
# (backend-0.0.1-SNAPSHOT.jar for this project)
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar app.jar

# Ensure the non-root user owns the file
RUN chown cartnest:cartnest app.jar

USER cartnest

# ---------------------------------------------------------------
# Render (and most cloud platforms) inject PORT at runtime.
# Spring Boot reads SERVER_PORT automatically via its relaxed-
# binding rules, so we can pass the env var directly.
# The default of 8080 matches your application.properties.
# ---------------------------------------------------------------
ENV SERVER_PORT=8080
EXPOSE 8080

# Use exec form so the JVM receives OS signals (SIGTERM) properly
ENTRYPOINT ["java", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-Dserver.port=${SERVER_PORT}", \
            "-jar", "app.jar"]
