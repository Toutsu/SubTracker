import asyncio
import os
from typing import Dict, Optional, Any
from dataclasses import dataclass, asdict
from enum import Enum
import aiohttp
from aiohttp import ClientSession, web
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
    billing_period: str
    next_payment: str
    category: str
    is_active: bool
    description: Optional[str] = None


@dataclass
class CreateSubscriptionRequest:
    user_id: str
    name: str
    price: str
    currency: str
    billing_period: str
    next_payment: str
    category: str
    description: Optional[str] = None


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
    ADDING_SUBSCRIPTION_CATEGORY = State()
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
        self.api_base_url = os.getenv("BACKEND_API_URL", "http://localhost:8080")
        
        # Хранилище состояний пользователей
        self.user_tokens: Dict[int, str] = {}  # chat_id -> JWT token
        
        # Инициализация веб-сервера для health check
        self.app = web.Application()
        self.app.router.add_get('/health', self.health_check)
        
        # Регистрация обработчиков
        self.register_handlers()
    
    def camel_to_snake(self, name: str) -> str:
        """Преобразование camelCase в snake_case"""
        import re
        # Вставка подчеркивания перед заглавными буквами, которые не в начале строки
        s1 = re.sub('(.)([A-Z][a-z]+)', r'\1_\2', name)
        # Вставка подчеркивания перед заглавными буквами, за которыми следует строчная или цифра
        return re.sub('([a-z0-9])([A-Z])', r'\1_\2', s1).lower()

    def snake_to_camel(self, name: str) -> str:
        """Преобразование snake_case в camelCase"""
        components = name.split('_')
        result = components[0] + ''.join(x.capitalize() for x in components[1:])
        print(f"snake_to_camel: {name} -> {result}")
        return result

    def convert_keys_to_camel(self, data: Dict[str, Any]) -> Dict[str, Any]:
        """Рекурсивное преобразование ключей словаря из snake_case в camelCase"""
        print(f"convert_keys_to_camel called with: {data}")
        if isinstance(data, dict):
            result = {self.snake_to_camel(k): self.convert_keys_to_camel(v) for k, v in data.items()}
            print(f"convert_keys_to_camel result: {result}")
            return result
        elif isinstance(data, list):
            return [self.convert_keys_to_camel(item) for item in data]
        else:
            return data

    def convert_keys(self, data: Dict[str, Any]) -> Dict[str, Any]:
        """Рекурсивное преобразование ключей словаря из camelCase в snake_case"""
        if isinstance(data, dict):
            return {self.camel_to_snake(k): self.convert_keys(v) for k, v in data.items()}
        elif isinstance(data, list):
            return [self.convert_keys(item) for item in data]
        else:
            return data
    
    async def health_check(self, request):
        """Health check endpoint"""
        return web.json_response({
            "status": "UP",
            "timestamp": int(datetime.now().timestamp() * 1000),
            "version": "1.0.0",
            "component": "telegram-bot"
        })
    
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
        self.dp.message(BotState.ADDING_SUBSCRIPTION_CATEGORY)(self.handle_subscription_category)
        # Обработка callback-запросов
        self.dp.callback_query()(self.handle_callback_query)
        self.dp.message(BotState.ADDING_SUBSCRIPTION_DATE)(self.handle_subscription_date)
        
        # Обработка всех остальных текстовых сообщений
        self.dp.message(F.text)(self.handle_text)
    
    async def set_bot_commands(self):
        """Установка списка команд бота"""
        from aiogram.types import BotCommand
        
        commands = [
            BotCommand(command="start", description="Запустить бота"),
            BotCommand(command="help", description="Показать справку"),
            BotCommand(command="login", description="Войти в систему"),
            BotCommand(command="list", description="Показать все подписки"),
            BotCommand(command="add", description="Добавить новую подписку"),
            BotCommand(command="delete", description="Удалить подписку"),
            BotCommand(command="stats", description="Показать статистику расходов")
        ]
        
        await self.bot.set_my_commands(commands)
    
    async def start(self):
        """Запуск бота"""
        print("Starting Telegram Bot...")
        
        # Запуск веб-сервера в отдельной задаче
        runner = web.AppRunner(self.app)
        await runner.setup()
        # Установка команд бота
        await self.set_bot_commands()
        site = web.TCPSite(runner, '0.0.0.0', 8081)
        await site.start()
        
        print("Health check server is running on port 8081")
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

💡 Вы также можете использовать меню команд внизу экрана для быстрого доступа к функциям бота.
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
                    f"{self.api_base_url}/api/login",
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
                        subscriptions = [Subscription(**self.convert_keys(sub)) for sub in subscriptions_data]
                        
                        if not subscriptions:
                            await message.answer("📋 У вас пока нет подписок.\nДобавьте первую: /add")
                        else:
                            message_text = "📋 Ваши подписки:\n"
                            for sub in subscriptions:
                                message_text += f"• {sub.name} - {sub.price} {sub.currency} ({sub.billing_period})\n"
                                message_text += f"  ID: {sub.id} | Следующий платеж: {sub.next_payment}\n"
                                message_text += f"  Категория: {sub.category}\n"
                            
                            await message.answer(message_text)
                    else:
                        await message.answer("❌ Ошибка при получении списка подписок")
        except Exception as e:
            print(f"List subscriptions error: {e}")
            await message.answer("❌ Ошибка соединения с сервером")
    
    async def handle_subscription_category(self, message: Message, state: FSMContext):
        """Обработка ввода категории подписки"""
        await state.update_data(category=message.text)
        await state.set_state(BotState.ADDING_SUBSCRIPTION_DATE)
        await message.answer("📅 Введите дату следующего платежа (YYYY-MM-DD):")
    
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
        
        # Отправляем кнопки для выбора валюты
        from aiogram.types import InlineKeyboardMarkup, InlineKeyboardButton
        keyboard = InlineKeyboardMarkup(inline_keyboard=[
            [InlineKeyboardButton(text="USD", callback_data="currency_USD")],
            [InlineKeyboardButton(text="EUR", callback_data="currency_EUR")],
            [InlineKeyboardButton(text="RUB", callback_data="currency_RUB")]
        ])
        await message.answer("💱 Выберите валюту:", reply_markup=keyboard)
    
    async def handle_subscription_currency(self, message: Message, state: FSMContext):
        """Обработка ввода валюты подписки"""
        await state.update_data(name=message.text)
        await state.set_state(BotState.ADDING_SUBSCRIPTION_PRICE)
        await message.answer("💰 Введите стоимость подписки:")
    
    async def handle_callback_query(self, callback_query: CallbackQuery, state: FSMContext):
        """Обработка callback-запросов от кнопок"""
        data = callback_query.data
        message = callback_query.message
        
        # Обработка выбора валюты
        if data.startswith("currency_"):
            currency = data.split("_")[1]
            supported_currencies = ["USD", "EUR", "RUB"]
            if currency in supported_currencies:
                await state.update_data(currency=currency)
                await state.set_state(BotState.ADDING_SUBSCRIPTION_CYCLE)
                await callback_query.answer()
                await message.edit_text(f"💱 Валюта выбрана: {currency}")
                
                # Отправляем кнопки для выбора цикла оплаты
                from aiogram.types import InlineKeyboardMarkup, InlineKeyboardButton
                keyboard = InlineKeyboardMarkup(inline_keyboard=[
                    [InlineKeyboardButton(text="Ежемесячно", callback_data="cycle_monthly")],
                    [InlineKeyboardButton(text="Ежегодно", callback_data="cycle_yearly")]
                ])
                await message.answer("🔄 Выберите цикл оплаты:", reply_markup=keyboard)
            else:
                await callback_query.answer("❌ Неподдерживаемая валюта")
        
        # Обработка выбора цикла оплаты
        elif data.startswith("cycle_"):
            cycle_map = {
                "cycle_monthly": "monthly",
                "cycle_yearly": "yearly"
            }
            if data in cycle_map:
                await state.update_data(billing_cycle=cycle_map[data])
                await state.set_state(BotState.ADDING_SUBSCRIPTION_CATEGORY)
                await callback_query.answer()
                await message.edit_text("🔄 Цикл оплаты выбран: " + ("ежемесячно" if data == "cycle_monthly" else "ежегодно"))
                
                # Отправляем кнопки для выбора категории
                from aiogram.types import InlineKeyboardMarkup, InlineKeyboardButton
                keyboard = InlineKeyboardMarkup(inline_keyboard=[
                    [InlineKeyboardButton(text="Развлечения", callback_data="category_entertainment")],
                    [InlineKeyboardButton(text="Продуктивность", callback_data="category_productivity")],
                    [InlineKeyboardButton(text="Дизайн", callback_data="category_design")],
                    [InlineKeyboardButton(text="Облачные сервисы", callback_data="category_cloud")],
                    [InlineKeyboardButton(text="Музыка", callback_data="category_music")],
                    [InlineKeyboardButton(text="Видео", callback_data="category_video")],
                    [InlineKeyboardButton(text="Другое", callback_data="category_other")]
                ])
                await message.answer("📂 Выберите категорию подписки:", reply_markup=keyboard)
            else:
                await callback_query.answer("❌ Неподдерживаемый цикл оплаты")
        
        # Обработка выбора категории
        elif data.startswith("category_"):
            category_map = {
                "category_entertainment": "Entertainment",
                "category_productivity": "Productivity",
                "category_design": "Design",
                "category_cloud": "Cloud Services",
                "category_music": "Music",
                "category_video": "Video",
                "category_other": "Other"
            }
            if data in category_map:
                await state.update_data(category=category_map[data])
                await state.set_state(BotState.ADDING_SUBSCRIPTION_DATE)
                await callback_query.answer()
                await message.edit_text("📂 Категория выбрана: " + category_map[data])
                await message.answer("📅 Введите дату следующего платежа (YYYY-MM-DD):")
            else:
                await callback_query.answer("❌ Неподдерживаемая категория")
    
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
                billing_period=subscription_data["billing_cycle"],
                next_payment=subscription_data["next_payment_date"],
                category=subscription_data.get("category", "Other")
            )
            
            async with aiohttp.ClientSession() as session:
                # Отладочный вывод
                request_dict = asdict(create_request)
                print(f"Request dict before conversion: {request_dict}")
                print(f"Testing snake_to_camel: billing_period -> {self.snake_to_camel('billing_period')}")
                # Тестовое преобразование
                test_dict = {"billing_period": "monthly", "name": "Test", "price": "10"}
                print(f"Test dict before conversion: {test_dict}")
                test_converted = self.convert_keys_to_camel(test_dict)
                print(f"Test dict after conversion: {test_converted}")
                converted_dict = self.convert_keys_to_camel(request_dict)
                print(f"Request dict after conversion: {converted_dict}")
                
                # Явная сериализация в JSON для проверки
                import json as json_module
                json_data = json_module.dumps(converted_dict)
                print(f"JSON data being sent: {json_data}")
                
                async with session.post(
                    f"{self.api_base_url}/api/subscriptions",
                    data=json_data,
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
                        subscriptions = [Subscription(**self.convert_keys(sub)) for sub in subscriptions_data]
                        
                        if not subscriptions:
                            await message.answer("📊 У вас пока нет подписок для анализа")
                        else:
                            # Рассчитываем статистику
                            total_monthly = sum(float(sub.price) for sub in subscriptions)
                            
                            total_yearly = 0
                            for sub in subscriptions:
                                price = float(sub.price)
                                if sub.billing_period.lower() == "monthly":
                                    total_yearly += price * 12
                                elif sub.billing_period.lower() == "yearly":
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