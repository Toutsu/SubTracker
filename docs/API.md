# API SubTracker

## 📡 API Reference

### Authentication Endpoints

#### POST /api/register
Регистрация нового пользователя

**Request Body:**
```json
{
    "username": "string",
    "email": "string", 
    "password": "string"
}
```

**Response:**
```json
{
    "success": boolean,
    "message": "string"
}
```

**Status Codes:**
- `200` - Успешная регистрация
- `400` - Пользователь уже существует
- `500` - Ошибка сервера

#### POST /api/login
Аутентификация пользователя

**Request Body:**
```json
{
    "username": "string",
    "password": "string"
}
```

**Response:**
```json
{
    "success": boolean,
    "message": "string",
    "token": "string" // JWT token
}
```

**Status Codes:**
- `200` - Успешная аутентификация
- `401` - Неверные учетные данные
- `500` - Ошибка сервера

### Subscription Endpoints (Protected)

#### GET /api/subscriptions
Получение всех подписок

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Response:**
```json
[
    {
        "id": "string",
        "userId": "string",
        "name": "string",
        "price": "string",
        "currency": "string",
        "billingCycle": "monthly|yearly|weekly",
        "nextPaymentDate": "YYYY-MM-DD",
        "isActive": boolean
    }
]
```

#### GET /api/subscriptions/{userId}
Получение подписок конкретного пользователя

**Parameters:**
- `userId` (path) - ID пользователя

**Headers:**
```
Authorization: Bearer <jwt_token>
```

#### POST /api/subscriptions
Создание новой подписки

**Headers:**
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
    "userId": "string",
    "name": "string",
    "price": "string",
    "currency": "string",
    "billingCycle": "monthly|yearly|weekly",
    "nextPaymentDate": "YYYY-MM-DD"
}
```

**Response:**
```json
{
    "id": "string",
    "userId": "string",
    "name": "string",
    "price": "string",
    "currency": "string",
    "billingCycle": "string",
    "nextPaymentDate": "string",
    "isActive": true
}
```

#### PUT /api/subscriptions/{id}
Обновление подписки

**Parameters:**
- `id` (path) - ID подписки

**Headers:**
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
    "name": "string",
    "price": "string",
    "currency": "string",
    "billingCycle": "monthly|yearly|weekly",
    "nextPaymentDate": "YYYY-MM-DD"
}
```

**Response:**
```json
{
    "id": "string",
    "userId": "string",
    "name": "string",
    "price": "string",
    "currency": "string",
    "billingCycle": "string",
    "nextPaymentDate": "string",
    "isActive": true
}
```

#### DELETE /api/subscriptions/{id}
Удаление подписки

**Parameters:**
- `id` (path) - ID подписки

**Headers:**
```
Authorization: Bearer <jwt_token>
```

**Status Codes:**
- `204` - Успешное удаление
- `404` - Подписка не найдена
- `401` - Неавторизован

### Health Check

#### GET /health
Проверка состояния сервера

**Response:**
```json
{
    "status": "UP",
    "timestamp": 1703123456789,
    "version": "1.0.0",
    "database": "SQLite"
}
```

## 🔌 Примеры использования API

### Вход в систему
```bash
curl -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"user"}'
```

### Получение подписок (с токеном)
```bash
curl -X GET http://localhost:8080/api/subscriptions \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Регистрация нового пользователя
```bash
curl -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"password123"}'
```

### Создание подписки
```bash
curl -X POST http://localhost:8080/api/subscriptions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "userId": "user123",
    "name": "Netflix",
    "price": "15.99",
    "currency": "USD",
    "billingCycle": "monthly",
    "nextPaymentDate": "2024-12-25"
  }'
```

### Обновление подписки
```bash
curl -X PUT http://localhost:8080/api/subscriptions/subscription123 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Netflix Premium",
    "price": "19.99",
    "currency": "USD",
    "billingCycle": "monthly",
    "nextPaymentDate": "2024-12-25"
  }'
```

### Удаление подписки
```bash
curl -X DELETE http://localhost:8080/api/subscriptions/subscription123 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"