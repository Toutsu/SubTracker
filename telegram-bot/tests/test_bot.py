import pytest
import asyncio
from unittest.mock import Mock, AsyncMock, patch
from bot import SubTrackerBot, BotState, User, Subscription, LoginRequest, LoginResponse, CreateSubscriptionRequest
from aiogram.types import Message, Chat, User as TelegramUser
from aiogram.fsm.context import FSMContext
from aiogram.fsm.storage.base import StorageKey

class TestSubTrackerBot:
    """Тесты для Telegram бота SubTracker"""
    
    @pytest.fixture
    def bot(self):
        """Фикстура для создания экземпляра бота"""
        with patch.dict('os.environ', {'TELEGRAM_BOT_TOKEN': '123456789:ABCdefGhIJKlmNoPQRsTUVwxyZ'}):
            with patch('bot.Bot') as mock_bot:
                mock_bot_instance = Mock()
                mock_bot.return_value = mock_bot_instance
                bot_instance = SubTrackerBot()
                bot_instance.bot = mock_bot_instance
                return bot_instance
    
    @pytest.fixture
    def message(self):
        """Фикстура для создания тестового сообщения"""
        chat = Mock()
        chat.id = 12345
        
        user = Mock(spec=TelegramUser)
        user.id = 12345
        user.username = "test_user"
        
        msg = Mock(spec=Message)
        msg.chat = chat
        msg.from_user = user
        msg.text = "test message"
        return msg
    
    @pytest.fixture
    def state(self):
        """Фикстура для создания FSM состояния"""
        storage = AsyncMock()
        storage_key = StorageKey(bot_id=123, chat_id=12345, user_id=12345)
        return FSMContext(storage=storage, key=storage_key)
    
    def test_bot_initialization(self, bot):
        """Тест инициализации бота"""
        assert bot.bot_token == "123456789:ABCdefGhIJKlmNoPQRsTUVwxyZ"
        assert bot.api_base_url == "http://localhost:8080"
        assert isinstance(bot.user_tokens, dict)
    
    def test_bot_initialization_without_token(self):
        """Тест инициализации бота без токена"""
        with patch.dict('os.environ', {}, clear=True):
            with pytest.raises(ValueError, match="TELEGRAM_BOT_TOKEN environment variable is required"):
                SubTrackerBot()
    
    @pytest.mark.asyncio
    async def test_handle_start_command(self, bot, message, state):
        """Тест обработчика команды /start"""
        # Мокаем метод answer
        message.answer = AsyncMock()
        state.set_state = AsyncMock()
        
        await bot.handle_start_command(message, state)
        
        # Проверяем, что был вызван метод answer с правильным сообщением
        message.answer.assert_called_once()
        args, kwargs = message.answer.call_args
        assert "Добро пожаловать в SubTracker" in args[0]
        
        # Проверяем, что состояние установлено в NONE
        state.set_state.assert_called_once_with(BotState.NONE)
    
    @pytest.mark.asyncio
    async def test_handle_help_command(self, bot, message):
        """Тест обработчика команды /help"""
        # Мокаем метод answer
        message.answer = AsyncMock()
        
        await bot.handle_help_command(message)
        
        # Проверяем, что был вызван метод answer с правильным сообщением
        message.answer.assert_called_once()
        args, kwargs = message.answer.call_args
        assert "Справка по командам" in args[0]
    
    @pytest.mark.asyncio
    async def test_handle_login_command(self, bot, message, state):
        """Тест обработчика команды /login"""
        # Мокаем метод answer
        message.answer = AsyncMock()
        state.set_state = AsyncMock()
        
        await bot.handle_login_command(message, state)
        
        # Проверяем, что состояние установлено в LOGIN_USERNAME
        state.set_state.assert_called_once_with(BotState.LOGIN_USERNAME)
        
        # Проверяем, что был отправлен запрос имени пользователя
        message.answer.assert_called_once_with("👤 Введите имя пользователя:")
    
    @pytest.mark.asyncio
    async def test_handle_login_username(self, bot, message, state):
        """Тест обработки ввода имени пользователя при логине"""
        # Мокаем методы
        state.update_data = AsyncMock()
        state.set_state = AsyncMock()
        message.answer = AsyncMock()
        
        await bot.handle_login_username(message, state)
        
        # Проверяем, что имя пользователя сохранено
        state.update_data.assert_called_once_with(username="test message")
        
        # Проверяем, что состояние изменено на LOGIN_PASSWORD
        state.set_state.assert_called_once_with(BotState.LOGIN_PASSWORD)
        
        # Проверяем, что был отправлен запрос пароля
        message.answer.assert_called_once_with("🔑 Введите пароль:")
    
    @pytest.mark.asyncio
    async def test_handle_login_password(self, bot, message, state):
        """Тест обработки ввода пароля при логине"""
        # Мокаем методы
        state.get_data = AsyncMock(return_value={"username": "test_user"})
        state.set_state = AsyncMock()
        state.clear = AsyncMock()
        message.answer = AsyncMock()
        
        with patch.object(bot, 'handle_login', new=AsyncMock()) as mock_handle_login:
            await bot.handle_login_password(message, state)
            
            # Проверяем, что вызван метод handle_login с правильными параметрами
            mock_handle_login.assert_called_once_with(message, "test_user", "test message")
            
            # Проверяем, что состояние установлено в NONE и очищено
            state.set_state.assert_called_once_with(BotState.NONE)
            state.clear.assert_called_once()
    
    @pytest.mark.asyncio
    async def test_handle_add_command_without_auth(self, bot, message, state):
        """Тест обработчика команды /add без аутентификации"""
        # Мокаем метод answer
        message.answer = AsyncMock()
        
        # Убеждаемся, что токен не установлен
        bot.user_tokens = {}
        
        await bot.handle_add_command(message, state)
        
        # Проверяем, что был отправлен запрос на вход
        message.answer.assert_called_once_with("❌ Сначала выполните вход: /login")
    
    @pytest.mark.asyncio
    async def test_handle_add_command_with_auth(self, bot, message, state):
        """Тест обработчика команды /add с аутентификацией"""
        # Мокаем методы
        message.answer = AsyncMock()
        state.set_state = AsyncMock()
        
        # Устанавливаем токен для пользователя
        bot.user_tokens[12345] = "test_token"
        
        await bot.handle_add_command(message, state)
        
        # Проверяем, что состояние установлено в ADDING_SUBSCRIPTION_NAME
        state.set_state.assert_called_once_with(BotState.ADDING_SUBSCRIPTION_NAME)
        
        # Проверяем, что был отправлен запрос названия подписки
        message.answer.assert_called_once_with("📝 Введите название подписки:")
    
    @pytest.mark.asyncio
    async def test_handle_subscription_name(self, bot, message, state):
        """Тест обработки ввода названия подписки"""
        # Мокаем методы
        state.update_data = AsyncMock()
        state.set_state = AsyncMock()
        message.answer = AsyncMock()
        
        await bot.handle_subscription_name(message, state)
        
        # Проверяем, что название сохранено
        state.update_data.assert_called_once_with(name="test message")
        
        # Проверяем, что состояние изменено на ADDING_SUBSCRIPTION_PRICE
        state.set_state.assert_called_once_with(BotState.ADDING_SUBSCRIPTION_PRICE)
        
        # Проверяем, что был отправлен запрос стоимости
        message.answer.assert_called_once_with("💰 Введите стоимость подписки:")
    
    @pytest.mark.asyncio
    async def test_handle_subscription_price(self, bot, message, state):
        """Тест обработки ввода стоимости подписки"""
        # Мокаем методы
        state.update_data = AsyncMock()
        state.set_state = AsyncMock()
        message.answer = AsyncMock()
        
        await bot.handle_subscription_price(message, state)
        
        # Проверяем, что стоимость сохранена
        state.update_data.assert_called_once_with(price="test message")
        
        # Проверяем, что состояние изменено на ADDING_SUBSCRIPTION_CURRENCY
        state.set_state.assert_called_once_with(BotState.ADDING_SUBSCRIPTION_CURRENCY)
        
        # Проверяем, что был отправлен запрос валюты
        message.answer.assert_called_once_with("💱 Введите валюту (например: USD, EUR, RUB):")
    
    @pytest.mark.asyncio
    async def test_handle_subscription_currency(self, bot, message, state):
        """Тест обработки ввода валюты подписки"""
        # Мокаем методы
        state.update_data = AsyncMock()
        state.set_state = AsyncMock()
        message.answer = AsyncMock()
        
        await bot.handle_subscription_currency(message, state)
        
        # Проверяем, что валюта сохранена
        state.update_data.assert_called_once_with(currency="test message")
        
        # Проверяем, что состояние изменено на ADDING_SUBSCRIPTION_CYCLE
        state.set_state.assert_called_once_with(BotState.ADDING_SUBSCRIPTION_CYCLE)
        
        # Проверяем, что был отправлен запрос цикла оплаты
        message.answer.assert_called_once_with("🔄 Введите цикл оплаты (например: monthly, yearly):")
    
    @pytest.mark.asyncio
    async def test_handle_subscription_cycle(self, bot, message, state):
        """Тест обработки ввода цикла оплаты подписки"""
        # Мокаем методы
        state.update_data = AsyncMock()
        state.set_state = AsyncMock()
        message.answer = AsyncMock()
        
        await bot.handle_subscription_cycle(message, state)
        
        # Проверяем, что цикл оплаты сохранен
        state.update_data.assert_called_once_with(billing_cycle="test message")
        
        # Проверяем, что состояние изменено на ADDING_SUBSCRIPTION_DATE
        state.set_state.assert_called_once_with(BotState.ADDING_SUBSCRIPTION_DATE)
        
        # Проверяем, что был отправлен запрос даты следующего платежа
        message.answer.assert_called_once_with("📅 Введите дату следующего платежа (YYYY-MM-DD):")
    
    @pytest.mark.asyncio
    async def test_handle_subscription_date(self, bot, message, state):
        """Тест обработки ввода даты следующего платежа"""
        # Мокаем методы
        state.update_data = AsyncMock()
        state.get_data = AsyncMock(return_value={
            "name": "Test Subscription",
            "price": "9.99",
            "currency": "USD",
            "billing_cycle": "monthly",
            "next_payment_date": "2023-12-31"
        })
        state.set_state = AsyncMock()
        state.clear = AsyncMock()
        message.answer = AsyncMock()
        
        with patch.object(bot, 'create_subscription', new=AsyncMock()) as mock_create_subscription:
            await bot.handle_subscription_date(message, state)
            
            # Проверяем, что дата сохранена
            state.update_data.assert_called_once_with(next_payment_date="test message")
            
            # Проверяем, что вызван метод create_subscription
            mock_create_subscription.assert_called_once()
            
            # Проверяем, что состояние установлено в NONE и очищено
            state.set_state.assert_called_once_with(BotState.NONE)
            state.clear.assert_called_once()
    
    @pytest.mark.asyncio
    async def test_handle_delete_command_without_auth(self, bot, message):
        """Тест обработчика команды /delete без аутентификации"""
        from aiogram.filters import CommandObject
        
        # Мокаем метод answer
        message.answer = AsyncMock()
        
        # Убеждаемся, что токен не установлен
        bot.user_tokens = {}
        
        command = Mock(spec=CommandObject)
        command.args = "test_id"
        
        await bot.handle_delete_command(message, command)
        
        # Проверяем, что был отправлен запрос на вход
        message.answer.assert_called_once_with("❌ Сначала выполните вход: /login")
    
    @pytest.mark.asyncio
    async def test_handle_delete_command_without_args(self, bot, message):
        """Тест обработчика команды /delete без аргументов"""
        from aiogram.filters import CommandObject
        
        # Мокаем метод answer
        message.answer = AsyncMock()
        
        # Устанавливаем токен для пользователя
        bot.user_tokens[12345] = "test_token"
        
        command = Mock(spec=CommandObject)
        command.args = None
        
        await bot.handle_delete_command(message, command)
        
        # Проверяем, что был отправлен запрос ID подписки
        message.answer.assert_called_once_with("❌ Укажите ID подписки.\nПример: /delete abc123")
    
    @pytest.mark.asyncio
    async def test_handle_stats_command_without_auth(self, bot, message):
        """Тест обработчика команды /stats без аутентификации"""
        # Мокаем метод answer
        message.answer = AsyncMock()
        
        # Убеждаемся, что токен не установлен
        bot.user_tokens = {}
        
        await bot.handle_stats_command(message)
        
        # Проверяем, что был отправлен запрос на вход
        message.answer.assert_called_once_with("❌ Сначала выполните вход: /login")
    
    @pytest.mark.asyncio
    async def test_handle_text(self, bot, message, state):
        """Тест обработчика текстовых сообщений"""
        # Мокаем метод answer
        message.answer = AsyncMock()
        
        await bot.handle_text(message, state)
        
        # Проверяем, что был отправлен запрос на использование команд
        message.answer.assert_called_once_with("❓ Используйте команды для взаимодействия. /help - для справки")

if __name__ == "__main__":
    pytest.main([__file__, "-v"])