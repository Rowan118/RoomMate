FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY code/backend/pom.xml .
COPY code/backend/src ./src
RUN mvn -B -q package -DskipTests

# ---- Stage 2: small runtime image ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/roommate-backend-*.jar app.jar
COPY code/frontend ./frontend
COPY conf ./configuration

ENV PORT=8080 \
    STATIC_DIR=/app/frontend \
    CONF_DIR=/app/configuration \
    LOGIN_DB=/data/logins.db \
    PREF_DB=/data/preferences.db
RUN mkdir -p /data

EXPOSE 8080
CMD ["java", "-cp", "app.jar", "com.roommate.Main"]