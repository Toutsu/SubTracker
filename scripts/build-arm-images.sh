#!/bin/bash

# Скрипт для сборки Docker образов под архитектуру ARM (Raspberry Pi)

set -e  # Остановить выполнение при ошибке

echo "Начинаем сборку Docker образов для ARM архитектуры..."

# Создаем директорию для данных если её нет
mkdir -p data

# Сборка backend образа для ARM
echo "Сборка backend образа..."
docker buildx build --platform linux/arm/v7 -t subtracker-backend:arm-latest ./backend --load

# Сборка web-frontend образа для ARM
echo "Сборка web-frontend образа..."
docker buildx build --platform linux/arm/v7 -t subtracker-web-frontend:arm-latest ./web-frontend --load

# Сборка telegram-bot образа для ARM
echo "Сборка telegram-bot образа..."
docker buildx build --platform linux/arm/v7 -t subtracker-telegram-bot:arm-latest ./telegram-bot --load

echo "Все образы успешно собраны для ARM архитектуры!"
echo "Для загрузки образов в реестр используйте команду:"
echo "  docker push [имя_образа]:arm-latest"