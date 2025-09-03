#!/bin/bash

# Скрипт для развертывания приложения в Kubernetes

set -e  # Остановить выполнение при ошибке

echo "Начинаем развертывание приложения в Kubernetes..."

# Создание namespace если он не существует
echo "Создание namespace subtracker..."
kubectl create namespace subtracker --dry-run=client -o yaml | kubectl apply -f -

# Примение конфигураций
echo "Применение ConfigMap и Secret..."
kubectl apply -f k8s/configmap.yaml -n subtracker
kubectl apply -f k8s/secret.yaml -n subtracker

# Применение манифестов баз данных
echo "Применение манифестов баз данных..."
kubectl apply -f k8s/postgresql.yaml -n subtracker
kubectl apply -f k8s/redis.yaml -n subtracker

# Применение манифестов приложений
echo "Примение манифестов приложений..."
kubectl apply -f k8s/backend.yaml -n subtracker
kubectl apply -f k8s/web-frontend.yaml -n subtracker
kubectl apply -f k8s/telegram-bot.yaml -n subtracker

# Применение ingress
echo "Применение ingress..."
kubectl apply -f k8s/ingress.yaml -n subtracker

echo "Развертывание завершено!"
echo "Для проверки статуса используйте скрипт check-deployment.sh"