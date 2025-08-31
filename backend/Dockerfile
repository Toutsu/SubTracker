# Многоэтапная сборка для оптимизации размера образа
FROM gradle:8.4-jdk17 AS build

# Установка рабочей директории
WORKDIR /app

# Копирование файлов сборки
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle gradle
COPY gradlew gradlew.bat ./

# Копирование исходного кода всех модулей
COPY shared/ shared/
COPY backend/ backend/

# Предварительная загрузка зависимостей (для кэширования Docker слоев)
RUN gradle dependencies --no-daemon || true

# Сборка приложения
RUN gradle :backend:shadowJar --no-daemon

# Финальный образ
FROM openjdk:17-jre-slim

# Установка необходимых пакетов
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

# Создание пользователя для безопасности
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Создание рабочей директории
WORKDIR /app

# Создание директории для данных
RUN mkdir -p /app/data && chown -R appuser:appuser /app

# Копирование JAR файла из этапа сборки
COPY --from=build /app/backend/build/libs/backend.jar /app/app.jar

# Переключение на пользователя appuser
USER appuser

# Переменные окружения
ENV DATABASE_URL="jdbc:sqlite:/app/data/subtracker.db"
ENV DATABASE_DRIVER="org.sqlite.JDBC"
ENV DATABASE_USER=""
ENV DATABASE_PASSWORD=""
ENV JWT_SECRET=""
ENV JWT_ISSUER="SubTracker"
ENV JWT_AUDIENCE="users"
ENV JWT_REALM="SubTracker"

# Порт приложения
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# Команда запуска
CMD ["java", \
     "-Xmx512m", \
     "-Xms256m", \
     "-XX:+UseG1GC", \
     "-XX:+UseContainerSupport", \
     "-Djava.security.egd=file:/dev/./urandom", \
     "-jar", "/app/app.jar"]