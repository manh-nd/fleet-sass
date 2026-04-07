FROM eclipse-temurin:21-jre-alpine
LABEL maintainer="Fleet Engineering <platform@fleet.io>"
LABEL org.opencontainers.image.title="Fleet Notification Hub"
LABEL org.opencontainers.image.version="3.0.0"

# Non-root user for security
RUN addgroup -S fleet && adduser -S fleet -G fleet

WORKDIR /app

# Copy the Spring Boot fat jar
COPY infrastructure/build/libs/infrastructure-*.jar app.jar

RUN chown fleet:fleet app.jar

USER fleet

# JVM tuning for container environments
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=45s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health/liveness || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
