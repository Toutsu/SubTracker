# Многоэтапная сборка для оптимизации размера образа
FROM node:20-alpine AS deps

# Установка рабочей директории
WORKDIR /app

# Копирование файлов зависимостей
COPY package.json package-lock.json ./

# Установка зависимостей
RUN npm ci

# Этап сборки
FROM node:20-alpine AS builder

# Установка рабочей директории
WORKDIR /app

# Копирование зависимостей из предыдущего этапа
COPY --from=deps /app/node_modules ./node_modules
COPY package.json package-lock.json ./

# Создание директории public, если она отсутствует
RUN mkdir -p public

# Копирование исходного кода
COPY . .

# Сборка приложения
RUN npm run build

# Финальный образ
FROM node:20-alpine AS runner

# Установка рабочей директории
WORKDIR /app

# Создание пользователя для безопасности
RUN addgroup -g 1001 -S nodejs
RUN adduser -S nextjs -u 1001

# Копирование файлов сборки из этапа builder
COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static
COPY --from=builder /app/public ./public

# Переключение на пользователя nextjs
USER nextjs

# Порт приложения
EXPOSE 3000

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:3000/health || exit 1

# Команда запуска
CMD ["node", "server.js"]