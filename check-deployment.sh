#!/bin/bash

# Скрипт для проверки статуса развертывания в Kubernetes

echo "Проверка статуса развертывания в Kubernetes..."

# Проверка namespace
echo "=== Namespace ==="
kubectl get namespace subtracker

# Проверка pods
echo -e "\n=== Pods ==="
kubectl get pods -n subtracker

# Проверка services
echo -e "\n=== Services ==="
kubectl get services -n subtracker

# Проверка ingress
echo -e "\n=== Ingress ==="
kubectl get ingress -n subtracker

# Проверка deployments
echo -e "\n=== Deployments ==="
kubectl get deployments -n subtracker

# Проверка ConfigMap и Secret
echo -e "\n=== ConfigMaps ==="
kubectl get configmap -n subtracker

echo -e "\n=== Secrets ==="
kubectl get secret -n subtracker

# Проверка логов backend
echo -e "\n=== Логи backend ==="
kubectl logs deployment/backend -n subtracker --tail=20

# Проверка логов frontend
echo -e "\n=== Логи frontend ==="
kubectl logs deployment/web-frontend -n subtracker --tail=20

# Проверка логов telegram-bot
echo -e "\n=== Логи telegram-bot ==="
kubectl logs deployment/telegram-bot -n subtracker --tail=20

echo -e "\nПроверка завершена!"