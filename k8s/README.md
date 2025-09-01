# Деплой SubTracker в Kubernetes

Это руководство описывает процесс деплоя приложения SubTracker в кластер Kubernetes.

## Компоненты приложения

Приложение состоит из следующих компонентов:

1. **Backend** - API сервер на Kotlin/Spring Boot
2. **Web Frontend** - Веб-интерфейс на Next.js
3. **Telegram Bot** - Бот на Python/aiogram
4. **PostgreSQL** - База данных
5. **Redis** - Хранилище для состояний бота

## Требования

- Kubernetes кластер (минимум 1.21 версия)
- kubectl CLI
- kustomize CLI (опционально)

## Подготовка к деплою

1. Убедитесь, что у вас есть доступ к Kubernetes кластеру:
   ```bash
   kubectl cluster-info
   ```

2. Создайте namespace для приложения:
   ```bash
   kubectl create namespace subtracker
   ```

## Деплой приложения

### Вариант 1: Используя kustomize (рекомендуется)

1. Примените манифесты:
   ```bash
   kubectl apply -k k8s/ -n subtracker
   ```

### Вариант 2: Применяя манифесты по отдельности

1. Создайте ConfigMap:
   ```bash
   kubectl apply -f k8s/configmap.yaml -n subtracker
   ```

2. Создайте Secret:
   ```bash
   kubectl apply -f k8s/secret.yaml -n subtracker
   ```

3. Задеплойте базу данных:
   ```bash
   kubectl apply -f k8s/postgresql.yaml -n subtracker
   ```

4. Задеплойте Redis:
   ```bash
   kubectl apply -f k8s/redis.yaml -n subtracker
   ```

5. Задеплойте backend:
   ```bash
   kubectl apply -f k8s/backend.yaml -n subtracker
   ```

6. Задеплойте web-frontend:
   ```bash
   kubectl apply -f k8s/web-frontend.yaml -n subtracker
   ```

7. Задеплойте telegram-bot:
   ```bash
   kubectl apply -f k8s/telegram-bot.yaml -n subtracker
   ```

8. Создайте ingress:
   ```bash
   kubectl apply -f k8s/ingress.yaml -n subtracker
   ```

## Проверка статуса деплоя

1. Проверьте статус подов:
   ```bash
   kubectl get pods -n subtracker
   ```

2. Проверьте сервисы:
   ```bash
   kubectl get services -n subtracker
   ```

3. Проверьте ingress:
   ```bash
   kubectl get ingress -n subtracker
   ```

## Доступ к приложению

После успешного деплоя приложение будет доступно по следующим адресам:

- Веб-интерфейс: http://subtracker.local
- API: http://api.subtracker.local

Для локального тестирования добавьте следующие записи в файл `/etc/hosts` (или `C:\Windows\System32\drivers\etc\hosts` на Windows):

```
127.0.0.1 subtracker.local
127.0.0.1 api.subtracker.local
```

## Обновление приложения

Для обновления приложения:

1. Обновите Docker образы в кластере
2. Примените обновленные манифесты:
   ```bash
   kubectl apply -k k8s/ -n subtracker
   ```

## Удаление приложения

Для удаления приложения:

```bash
kubectl delete -k k8s/ -n subtracker
```

Или по отдельности:
```bash
kubectl delete -f k8s/ingress.yaml -n subtracker
kubectl delete -f k8s/telegram-bot.yaml -n subtracker
kubectl delete -f k8s/web-frontend.yaml -n subtracker
kubectl delete -f k8s/backend.yaml -n subtracker
kubectl delete -f k8s/redis.yaml -n subtracker
kubectl delete -f k8s/postgresql.yaml -n subtracker
kubectl delete -f k8s/secret.yaml -n subtracker
kubectl delete -f k8s/configmap.yaml -n subtracker
```

Затем удалите namespace:
```bash
kubectl delete namespace subtracker