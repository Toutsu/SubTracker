FROM python:3.11-slim

WORKDIR /app

# Копируем requirements файлы
COPY requirements.txt requirements-test.txt ./

# Устанавливаем зависимости
RUN pip install --no-cache-dir -r requirements.txt

# Копируем исходный код
COPY . .

# Создаем директорию для логов
RUN mkdir -p /app/logs

# Открываем порт для health check
EXPOSE 8081

# Команда для запуска приложения
CMD ["python", "bot.py"]