# ═══════════════════════════════════════════════════════════════════════════════
#  SH-POS System — Dockerfile
#  Multi-stage build:
#    Stage 1 (build) → compiles Java source with Maven
#    Stage 2 (run)   → lightweight JRE image runs the jar
# ═══════════════════════════════════════════════════════════════════════════════

# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy Maven wrapper and pom first — cached layer, only re-runs if pom changes
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source and build jar
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# ── Stage 2: Run ──────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Security — run as non-root user
RUN addgroup -S posgroup && adduser -S posuser -G posgroup

# Copy compiled jar from build stage
COPY --from=build /app/target/*.jar app.jar
RUN chown posuser:posgroup app.jar

USER posuser

EXPOSE 5500

ENTRYPOINT [ \
  "java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar" \
]