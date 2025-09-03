# Развертывание SubTracker на Raspberry Pi с использованием Kubernetes

Это руководство описывает процесс развертывания приложения SubTracker на Raspberry Pi с использованием Kubernetes.

## Требования

- Raspberry Pi 4 с 4GB+ RAM
- microSD карта на 32GB+
- Доступ к интернету
- Установленная ОС Raspberry Pi OS (64-bit рекомендуется)
- Установленные Docker и Kubernetes (k3s рекомендуется)

## Подготовка Raspberry Pi

### 1. Установка Docker

```bash
# Обновление системы
sudo apt update && sudo apt upgrade -y

# Установка Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Добавление пользователя в группу docker
sudo usermod -aG docker $USER

# Перезагрузка для применения изменений
sudo reboot
```

### 2. Установка k3s (облегченная версия Kubernetes)

```bash
# Установка k3s
curl -sfL https://get.k3s.io | sh -

# Включение k3s в автозагрузку
sudo systemctl enable k3s

# Проверка статуса
sudo systemctl status k3s
```

### 3. Настройка kubectl

```bash
# Копирование конфигурации k3s
mkdir -p ~/.kube
sudo cp /etc/rancher/k3s/k3s.yaml ~/.kube/config
sudo chown $USER:$USER ~/.kube/config

# Установка kubectl (если не установлен)
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/arm64/kubectl"
chmod +x kubectl
sudo mv kubectl /usr/local/bin/
```

## Сборка Docker образов для ARM

### 1. Настройка Docker Buildx

```bash
# Создание нового builder'а
docker buildx create --name mybuilder --use
docker buildx inspect --bootstrap
```

### 2. Сборка образов

```bash
# Использование подготовленного скрипта
chmod +x build-arm-images.sh
./build-arm-images.sh
```

## Развертывание в Kubernetes

### 1. Развертывание приложения

```bash
# Использование подготовленного скрипта
chmod +x deploy-k8s.sh
./deploy-k8s.sh
```

### 2. Проверка статуса развертывания

```bash
# Использование подготовленного скрипта
chmod +x check-deployment.sh
./check-deployment.sh
```

## Доступ к приложению

После успешного развертывания приложение будет доступно по следующим адресам:

- **Web Frontend**: http://[IP_RASPBERRY_PI]/
- **Backend API**: http://[IP_RASPBERRY_PI]:8080/
- **Telegram Bot**: Запускается как фоновый сервис

## Устранение неполадок

### Проверка логов

```bash
# Проверка логов конкретного пода
kubectl logs -n subtracker [имя_пода]

# Проверка логов с последующим отслеживанием
kubectl logs -n subtracker [имя_пода] -f
```

### Перезапуск подов

```bash
# Перезапуск конкретного деплоймента
kubectl rollout restart deployment/[имя_деплоймента] -n subtracker
```

### Удаление и повторное развертывание

```bash
# Удаление всего приложения
kubectl delete namespace subtracker

# Повторное развертывание
./deploy-k8s.sh
```

## Обновление приложения

Для обновления приложения:

1. Обновите код из репозитория:
   ```bash
   git pull origin main
   ```

2. Пересоберите Docker образы:
   ```bash
   ./build-arm-images.sh
   ```

3. Обновите развертывание:
   ```bash
   ./deploy-k8s.sh
   ```

4. Проверьте статус:
   ```bash
   ./check-deployment.sh