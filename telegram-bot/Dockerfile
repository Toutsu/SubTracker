# Многоэтапная сборка для оптимизации размера образа
FROM python:3.11-alpine AS builder

# Установка рабочей директории
WORKDIR /app

# Копирование файлов зависимостей
COPY requirements.txt .

# Установка зависимостей
RUN apk add --no-cache gcc musl-dev libffi-dev && \
    pip install --no-cache-dir --user -r requirements.txt

# Финальный образ
FROM python:3.11-alpine

# Установка рабочей директории
WORKDIR /app

# Установка необходимых пакетов
RUN apk add --no-cache curl

# Создание пользователя для безопасности
RUN addgroup -S appuser && adduser -S appuser -G appuser

# Копирование зависимостей из этапа builder
COPY --from=builder /root/.local /home/appuser/.local

# Копирование исходного кода
COPY . .

# Переключение на пользователя appuser
USER appuser

# Установка правильного пути для Python
ENV PATH=/home/appuser/.local/bin:$PATH

# Команда запуска
CMD ["python", "run_bot.py"]