FROM gradle:8.14.3-jdk17 AS build

WORKDIR /app

# 라이브러리 캐싱 필요 (시간)
COPY build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon

COPY . .
RUN gradle clean build -x test --no-daemon

FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=build /app/build/libs/sparta-delivery-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
