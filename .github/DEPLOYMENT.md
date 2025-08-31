# 🚀 Настройка CI/CD для SubTracker

## 📋 Необходимые секреты GitHub

Для работы CI/CD pipeline необходимо добавить следующие секреты в настройках репозитория:

### 🐳 Docker Hub (для публикации образов)
```
DOCKERHUB_USERNAME - имя пользователя Docker Hub
DOCKERHUB_TOKEN - токен доступа Docker Hub
```

### 🖥️ Сервер деплоя (опционально)
```
DEPLOY_HOST - IP адрес или домен сервера
DEPLOY_USER - имя пользователя для SSH
SSH_PRIVATE_KEY - приватный SSH ключ для доступа к серверу
```

### 🔐 Безопасность (обязательно)
```
JWT_SECRET - секретный ключ для JWT аутентификации (минимум 32 символа)
```

### 📢 Уведомления (опционально)
```
SLACK_WEBHOOK_URL - webhook URL для уведомлений в Slack
```

## 🔧 Настройка Docker Hub

1. Зарегистрируйтесь на [Docker Hub](https://hub.docker.com/)
2. Создайте репозиторий `subtracker-backend`
3. Создайте Access Token:
   - Account Settings → Security → New Access Token
   - Скопируйте токен (он показывается только один раз)

## 🖥️ Настройка сервера деплоя

### Подготовка сервера:
```bash
# Установка Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# Создание директории для данных
sudo mkdir -p /opt/subtracker/data
sudo chown $USER:$USER /opt/subtracker/data

# Создание скрипта для бэкапов (опционально)
sudo tee /opt/subtracker/backup.sh > /dev/null <<'EOF'
#!/bin/bash
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
docker exec subtracker-backend cp /app/data/subtracker.db /app/data/subtracker.db.backup.$TIMESTAMP
echo "Backup created: subtracker.db.backup.$TIMESTAMP"
EOF

sudo chmod +x /opt/subtracker/backup.sh
```

### Генерация SSH ключа:
```bash
# На локальной машине
ssh-keygen -t rsa -b 4096 -C "github-actions@subtracker"

# Копирование публичного ключа на сервер
ssh-copy-id -i ~/.ssh/id_rsa.pub user@your-server.com

# Содержимое приватного ключа добавить в GitHub Secrets
cat ~/.ssh/id_rsa
```

## 🔄 Процесс CI/CD

### При push в main:
1. ✅ **Сборка и тестирование** - компиляция всех модулей
2. ✅ **Проверка качества** - линтеры и анализ безопасности
3. ✅ **Интеграционные тесты** - проверка API endpoints
4. ✅ **Сборка Docker образа** - публикация в Docker Hub
5. ✅ **Деплой** - развертывание на сервере
6. ✅ **Уведомления** - отчет о статусе деплоя

### При pull request:
1. ✅ **Сборка и тестирование**
2. ✅ **Проверка качества кода**
3. ✅ **Интеграционные тесты**

## 🐳 Ручной деплой через Docker

### Локальный запуск:
```bash
# Сборка и запуск всех сервисов
docker-compose up -d

# Запуск только backend
docker-compose up -d backend

# Запуск с Telegram ботом
docker-compose --profile with-bot up -d
```

### Продакшен деплой:
```bash
# Загрузка образа
docker pull your-username/subtracker-backend:latest

# Создание бэкапа текущей базы данных (если существует)
docker exec subtracker-backend cp /app/data/subtracker.db /app/data/subtracker.db.backup.$(date +%Y%m%d_%H%M%S) || true

# Запуск контейнера
docker run -d \
  --name subtracker-backend \
  --restart unless-stopped \
  -p 8080:8080 \
  -v /opt/subtracker/data:/app/data \
  -e JWT_SECRET="your-production-secret" \
  -e DATABASE_URL="jdbc:sqlite:/app/data/subtracker.db" \
  your-username/subtracker-backend:latest
```

## 🔍 Мониторинг

### Health checks:
- Backend: `http://your-server:8080/health`
- Frontend: `http://your-server:3000/health`

### Логи:
```bash
# Логи backend
docker logs subtracker-backend -f

# Логи всех сервисов
docker-compose logs -f
```

### Мониторинг состояния приложения:
```bash
# Проверка запущенных контейнеров
docker ps | grep subtracker

# Проверка использования ресурсов
docker stats subtracker-backend

# Проверка работоспособности API
curl -f http://localhost:8080/health
```

## 🔒 Безопасность

### Рекомендации для продакшена:
1. **Смените JWT_SECRET** на случайную строку (минимум 32 символа)
2. **Используйте HTTPS** с SSL сертификатами
3. **Настройте файрвол** (только порты 80, 443, 22)
4. **Регулярно обновляйте** Docker образы
5. **Делайте бэкапы** базы данных
6. **Ограничьте права доступа** к директориям данных

### Генерация безопасного JWT_SECRET:
```bash
# Генерация случайного 32-символьного ключа
openssl rand -hex 32
```

### Пример .env для продакшена:
```env
JWT_SECRET=super-secure-random-string-change-this-in-production
DATABASE_URL=jdbc:sqlite:/app/data/subtracker.db
DATABASE_DRIVER=org.sqlite.JDBC
DATABASE_USER=
DATABASE_PASSWORD=
```

## 📊 Статусы сборки

Бейдж статуса CI/CD:
```markdown
[![CI/CD Pipeline](https://github.com/username/SubTracker/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/username/SubTracker/actions/workflows/ci-cd.yml)
```

## 🆘 Troubleshooting

### Сборка не проходит:
1. Проверьте логи GitHub Actions
2. Убедитесь, что все секреты добавлены
3. Проверьте синтаксис workflow файла

### Деплой не работает:
1. Проверьте SSH подключение к серверу
2. Убедитесь, что Docker установлен на сервере
3. Проверьте права доступа к директориям
4. Убедитесь, что JWT_SECRET установлен

### Docker образ не собирается:
1. Проверьте Dockerfile синтаксис
2. Убедитесь, что все файлы доступны
3. Проверьте .dockerignore файл

### Приложение не запускается:
1. Проверьте логи контейнера: `docker logs subtracker-backend`
2. Убедитесь, что все обязательные переменные окружения установлены
3. Проверьте права доступа к файлам базы данных
