import asyncio
import os
from typing import Dict, Optional, Any
from dataclasses import dataclass, asdict
from enum import Enum
import aiohttp
from aiohttp import ClientSession
import json
from datetime import datetime
from dotenv import load_dotenv

# Загрузка переменных окружения из файла .env
load_dotenv()

from aiogram import Bot, Dispatcher, F
from aiogram.types import Message, CallbackQuery
from aiogram.filters import Command, CommandObject
from aiogram.fsm.state import State, StatesGroup
from aiogram.fsm.context import FSMContext
from aiogram.fsm.storage.memory import MemoryStorage


# Определение моделей данных
@dataclass
class User:
    id: str
    username: str
    email: str
    password_hash: str


class BillingCycle(Enum):
    MONTHLY = "monthly"
    YEARLY = "yearly"
    WEEKLY = "weekly"


@dataclass
class Subscription:
    id: str
    user_id: str
    name: str
    price: str
    currency: str
    billing_cycle: str
    next_payment_date: str
    is_active: bool = True


@dataclass
class CreateSubscriptionRequest:
    user_id: str
    name: str
    price: str
    currency: str
    billing_cycle: str
    next_payment_date: str


@dataclass
class LoginRequest:
    username: str
    password: str


@dataclass
class LoginResponse:
    token: str


# Определение состояний пользователя
class BotState(StatesGroup):
    NONE = State()
    ADDING_SUBSCRIPTION_NAME = State()
    ADDING_SUBSCRIPTION_PRICE = State()
    ADDING_SUBSCRIPTION_CURRENCY = State()
    ADDING_SUBSCRIPTION_CYCLE = State()
    ADDING_SUBSCRIPTION_DATE = State()
    LOGIN_USERNAME = State()
    LOGIN_PASSWORD = State()


class SubTrackerBot:
    def __init__(self):
        # Получение токена бота из переменных окружения
        self.bot_token = os.getenv("TELEGRAM_BOT_TOKEN")
        if not self.bot_token:
            raise ValueError("TELEGRAM_BOT_TOKEN environment variable is required")
        
        # Инициализация бота и диспетчера
        self.bot = Bot(token=self.bot_token)
        self.dp = Dispatcher(storage=MemoryStorage())
        
        # Базовый URL API
        self.api_base_url = os.getenv("BACKEND_API_URL", "http://backend:8080")
        
        # Хранилище состояний пользователей
        self.user_tokens: Dict[int, str] = {}  # chat_id -> JWT token
        
        # Регистрация обработчиков
        self.register_handlers()
    
    def register_handlers(self):
        """Регистрация обработчиков команд и сообщений"""
        # Команды
        self.dp.message(Command("start"))(self.handle_start_command)
        self.dp.message(Command("help"))(self.handle_help_command)
        self.dp.message(Command("list"))(self.handle_list_command)
        self.dp.message(Command("add"))(self.handle_add_command)
        self.dp.message(Command("delete"))(self.handle_delete_command)
        self.dp.message(Command("stats"))(self.handle_stats_command)
        self.dp.message(Command("login"))(self.handle_login_command)
        
        # Обработка текстовых сообщений в зависимости от состояния
        self.dp.message(BotState.LOGIN_USERNAME)(self.handle_login_username)
        self.dp.message(BotState.LOGIN_PASSWORD)(self.handle_login_password)
        self.dp.message(BotState.ADDING_SUBSCRIPTION_NAME)(self.handle_subscription_name)
        self.dp.message(BotState.ADDING_SUBSCRIPTION_PRICE)(self.handle_subscription_price)
        self.dp.message(BotState.ADDING_SUBSCRIPTION_CURRENCY)(self.handle_subscription_currency)
        self.dp.message(BotState.ADDING_SUBSCRIPTION_CYCLE)(self.handle_subscription_cycle)
        self.dp.message(BotState.ADDING_SUBSCRIPTION_DATE)(self.handle_subscription_date)
        
        # Обработка всех остальных текстовых сообщений
        self.dp.message(F.text)(self.handle_text)
    
    async def start(self):
        """Запуск бота"""
        print("Starting Telegram Bot...")
        print("Bot is running... Press Ctrl+C to stop")
        await self.dp.start_polling(self.bot)
    
    async def handle_start_command(self, message: Message, state: FSMContext):
        """Обработка команды /start"""
        welcome_message = """
🎉 Добро пожаловать в SubTracker!

Я помогу вам управлять подписками и контролировать расходы.

Для начала работы выполните вход:
🔐 /login - Войти в систему

Или создайте аккаунт через веб-интерфейс.
        """.strip()
        
        await message.answer(welcome_message)
        await state.set_state(BotState.NONE)
    
    async def handle_help_command(self, message: Message):
        """Обработка команды /help"""
        help_message = """
📖 Справка по командам:

🔐 /login - Войти в систему
📋 /list - Показать все ваши подписки
➕ /add - Добавить новую подписку
🗑 /delete [id] - Удалить подписку по ID
📊 /stats - Показать статистику расходов
❓ /help - Показать эту справку
        """.strip()
        
        await message.answer(help_message)
    
    async def handle_login_command(self, message: Message, state: FSMContext):
        """Обработка команды /login"""
        await state.set_state(BotState.LOGIN_USERNAME)
        await message.answer("👤 Введите имя пользователя:")
    
    async def handle_login_username(self, message: Message, state: FSMContext):
        """Обработка ввода имени пользователя при логине"""
        await state.update_data(username=message.text)
        await state.set_state(BotState.LOGIN_PASSWORD)
        await message.answer("🔑 Введите пароль:")
    
    async def handle_login_password(self, message: Message, state: FSMContext):
        """Обработка ввода пароля при логине"""
        # Получаем сохраненное имя пользователя
        user_data = await state.get_data()
        username = user_data.get("username", "")
        password = message.text
        
        # Выполняем логин
        await self.handle_login(message, username, password)
        await state.set_state(BotState.NONE)
        await state.clear()
    
    async def handle_login(self, message: Message, username: str, password: str):
        """Выполнение аутентификации пользователя"""
        try:
            login_request = LoginRequest(username=username, password=password)
            
            async with aiohttp.ClientSession() as session:
                async with session.post(
                    f"{self.api_base_url}/api/auth/login",
                    json=asdict(login_request),
                    headers={"Content-Type": "application/json"}
                ) as response:
                    if response.status == 200:
                        response_data = await response.json()
                        login_response = LoginResponse(token=response_data["token"])
                        self.user_tokens[message.chat.id] = login_response.token
                        
                        await message.answer("✅ Вход выполнен успешно!\nТеперь вы можете использовать все команды бота.")
                    else:
                        await message.answer("❌ Неверное имя пользователя или пароль")
        except Exception as e:
            print(f"Login error: {e}")
            await message.answer("❌ Ошибка соединения с сервером")
    
    async def handle_list_command(self, message: Message):
        """Обработка команды /list"""
        # Проверка аутентификации
        token = self.user_tokens.get(message.chat.id)
        if not token:
            await message.answer("❌ Сначала выполните вход: /login")
            return
        
        try:
            async with aiohttp.ClientSession() as session:
                async with session.get(
                    f"{self.api_base_url}/api/subscriptions",
                    headers={"Authorization": f"Bearer {token}"}
                ) as response:
                    if response.status == 200:
                        subscriptions_data = await response.json()
                        subscriptions = [Subscription(**sub) for sub in subscriptions_data]
                        
                        if not subscriptions:
                            await message.answer("📋 У вас пока нет подписок.\nДобавьте первую: /add")
                        else:
                            message_text = "📋 Ваши подписки:\n"
                            for sub in subscriptions:
                                message_text += f"• {sub.name} - {sub.price} {sub.currency} ({sub.billing_cycle})\n"
                                message_text += f"  ID: {sub.id} | Следующий платеж: {sub.next_payment_date}\n"
                            
                            await message.answer(message_text)
                    else:
                        await message.answer("❌ Ошибка при получении списка подписок")
        except Exception as e:
            print(f"List subscriptions error: {e}")
            await message.answer("❌ Ошибка соединения с сервером")
    
    async def handle_add_command(self, message: Message, state: FSMContext):
        """Обработка команды /add"""
        # Проверка аутентификации
        token = self.user_tokens.get(message.chat.id)
        if not token:
            await message.answer("❌ Сначала выполните вход: /login")
            return
        
        await state.set_state(BotState.ADDING_SUBSCRIPTION_NAME)
        await message.answer("📝 Введите название подписки:")
    
    async def handle_subscription_name(self, message: Message, state: FSMContext):
        """Обработка ввода названия подписки"""
        await state.update_data(name=message.text)
        await state.set_state(BotState.ADDING_SUBSCRIPTION_PRICE)
        await message.answer("💰 Введите стоимость подписки:")
    
    async def handle_subscription_price(self, message: Message, state: FSMContext):
        """Обработка ввода стоимости подписки"""
        await state.update_data(price=message.text)
        await state.set_state(BotState.ADDING_SUBSCRIPTION_CURRENCY)
        await message.answer("💱 Введите валюту (например: USD, EUR, RUB):")
    
    async def handle_subscription_currency(self, message: Message, state: FSMContext):
        """Обработка ввода валюты подписки"""
        await state.update_data(currency=message.text)
        await state.set_state(BotState.ADDING_SUBSCRIPTION_CYCLE)
        await message.answer("🔄 Введите цикл оплаты (например: monthly, yearly):")
    
    async def handle_subscription_cycle(self, message: Message, state: FSMContext):
        """Обработка ввода цикла оплаты подписки"""
        await state.update_data(billing_cycle=message.text)
        await state.set_state(BotState.ADDING_SUBSCRIPTION_DATE)
        await message.answer("📅 Введите дату следующего платежа (YYYY-MM-DD):")
    
    async def handle_subscription_date(self, message: Message, state: FSMContext):
        """Обработка ввода даты следующего платежа и создание подписки"""
        await state.update_data(next_payment_date=message.text)
        
        # Получаем все данные подписки
        subscription_data = await state.get_data()
        
        # Создаем подписку
        await self.create_subscription(message, subscription_data)
        
        # Сбрасываем состояние
        await state.set_state(BotState.NONE)
        await state.clear()
    
    async def create_subscription(self, message: Message, subscription_data: dict):
        """Создание новой подписки"""
        # Проверка аутентификации
        token = self.user_tokens.get(message.chat.id)
        if not token:
            await message.answer("❌ Ошибка авторизации")
            return
        
        try:
            # Создаем объект запроса
            create_request = CreateSubscriptionRequest(
                user_id=str(message.chat.id),
                name=subscription_data["name"],
                price=subscription_data["price"],
                currency=subscription_data["currency"],
                billing_cycle=subscription_data["billing_cycle"],
                next_payment_date=subscription_data["next_payment_date"]
            )
            
            async with aiohttp.ClientSession() as session:
                async with session.post(
                    f"{self.api_base_url}/api/subscriptions",
                    json=asdict(create_request),
                    headers={
                        "Authorization": f"Bearer {token}",
                        "Content-Type": "application/json"
                    }
                ) as response:
                    if response.status == 201:
                        await message.answer("✅ Подписка успешно создана!")
                    else:
                        await message.answer("❌ Ошибка при создании подписки")
        except Exception as e:
            print(f"Create subscription error: {e}")
            await message.answer("❌ Ошибка соединения с сервером")
    
    async def handle_delete_command(self, message: Message, command: CommandObject):
        """Обработка команды /delete"""
        # Проверка аутентификации
        token = self.user_tokens.get(message.chat.id)
        if not token:
            await message.answer("❌ Сначала выполните вход: /login")
            return
        
        # Проверка наличия аргумента
        if not command.args:
            await message.answer("❌ Укажите ID подписки.\nПример: /delete abc123")
            return
        
        subscription_id = command.args.strip()
        
        try:
            async with aiohttp.ClientSession() as session:
                async with session.delete(
                    f"{self.api_base_url}/api/subscriptions/{subscription_id}",
                    headers={"Authorization": f"Bearer {token}"}
                ) as response:
                    if response.status == 200:
                        await message.answer("✅ Подписка успешно удалена!")
                    else:
                        await message.answer("❌ Ошибка при удалении подписки")
        except Exception as e:
            print(f"Delete subscription error: {e}")
            await message.answer("❌ Ошибка соединения с сервером")
    
    async def handle_stats_command(self, message: Message):
        """Обработка команды /stats"""
        # Проверка аутентификации
        token = self.user_tokens.get(message.chat.id)
        if not token:
            await message.answer("❌ Сначала выполните вход: /login")
            return
        
        try:
            async with aiohttp.ClientSession() as session:
                async with session.get(
                    f"{self.api_base_url}/api/subscriptions",
                    headers={"Authorization": f"Bearer {token}"}
                ) as response:
                    if response.status == 200:
                        subscriptions_data = await response.json()
                        subscriptions = [Subscription(**sub) for sub in subscriptions_data]
                        
                        if not subscriptions:
                            await message.answer("📊 У вас пока нет подписок для анализа")
                        else:
                            # Рассчитываем статистику
                            total_monthly = sum(float(sub.price) for sub in subscriptions)
                            
                            total_yearly = 0
                            for sub in subscriptions:
                                price = float(sub.price)
                                if sub.billing_cycle.lower() == "monthly":
                                    total_yearly += price * 12
                                elif sub.billing_cycle.lower() == "yearly":
                                    total_yearly += price
                                else:
                                    total_yearly += price  # Для других циклов оставляем как есть
                            
                            # Находим самую дешевую и самую дорогую подписку
                            cheapest = min(subscriptions, key=lambda x: float(x.price))
                            most_expensive = max(subscriptions, key=lambda x: float(x.price))
                            
                            # Формируем сообщение со статистикой
                            stats_message = "📊 Статистика ваших подписок:\n"
                            stats_message += f"💰 Всего подписок: {len(subscriptions)}\n"
                            stats_message += f"💵 Общие расходы в месяц: ${total_monthly:.2f}\n"
                            stats_message += f"💵 Общие расходы в год: ${total_yearly:.2f}\n"
                            stats_message += f"💚 Самая дешевая: {cheapest.name} - {cheapest.price} {cheapest.currency}\n"
                            stats_message += f"💔 Самая дорогая: {most_expensive.name} - {most_expensive.price} {most_expensive.currency}\n"
                            
                            await message.answer(stats_message)
                    else:
                        await message.answer("❌ Ошибка при получении статистики")
        except Exception as e:
            print(f"Stats error: {e}")
            await message.answer("❌ Ошибка соединения с сервером")
    
    async def handle_text(self, message: Message, state: FSMContext):
        """Обработка всех остальных текстовых сообщений"""
        await message.answer("❓ Используйте команды для взаимодействия. /help - для справки")


async def main():
    """Главная функция для запуска бота"""
    try:
        bot = SubTrackerBot()
        await bot.start()
    except Exception as e:
        print(f"Error starting bot: {e}")


if __name__ == "__main__":
    asyncio.run(main())