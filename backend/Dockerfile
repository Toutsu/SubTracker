# Многоэтапная сборка для оптимизации размера образа
FROM maven:3.9.4-amazoncorretto-17 AS build

# Установка рабочей директории
WORKDIR /app

# Копирование файлов сборки
COPY pom.xml ./

# Предварительная загрузка зависимостей (для кэширования Docker слоев)
RUN mvn dependency:go-offline -B

# Копирование исходного кода
COPY src/ src/

# Сборка приложения
RUN mvn clean package -DskipTests

# Финальный образ
FROM amazoncorretto:17-alpine

# Установка необходимых пакетов
RUN apk add --no-cache curl

# Создание пользователя для безопасности
RUN addgroup -S appuser && adduser -S appuser -G appuser

# Создание рабочей директории
WORKDIR /app

# Создание директории для данных
RUN mkdir -p /app/data && chown -R appuser:appuser /app

# Копирование JAR файла из этапа сборки
COPY --from=build /app/target/subtracker-backend-0.0.1-SNAPSHOT.jar /app/app.jar

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