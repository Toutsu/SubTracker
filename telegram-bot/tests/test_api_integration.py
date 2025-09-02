import pytest
import asyncio
from unittest.mock import Mock, AsyncMock, patch
import aiohttp
from aiohttp import ClientResponse
from aioresponses import aioresponses
from bot import SubTrackerBot, LoginRequest, LoginResponse, CreateSubscriptionRequest, Subscription

class TestAPIIntegration:
    """Тесты для интеграции с API"""
    
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
        
        user = Mock()
        user.id = 12345
        user.username = "test_user"
        
        msg = Mock()
        msg.chat = chat
        msg.from_user = user
        msg.text = "test message"
        msg.answer = AsyncMock()
        return msg
    
    @pytest.mark.asyncio
    async def test_handle_login_success(self, bot, message):
        """Тест успешной аутентификации"""
        with aioresponses() as m:
            # Мокируем POST запрос к API аутентификации
            m.post(
                "http://localhost:8080/api/login",
                payload={"token": "test_jwt_token"},
                status=200
            )
            
            await bot.handle_login(message, "test_user", "test_password")
            
            # Проверяем, что токен сохранен
            assert bot.user_tokens[12345] == "test_jwt_token"
            
            # Проверяем, что отправлено сообщение об успешном входе
            message.answer.assert_called_once_with("✅ Вход выполнен успешно!\nТеперь вы можете использовать все команды бота.")
    
    @pytest.mark.asyncio
    async def test_handle_login_failure(self, bot, message):
        """Тест неудачной аутентификации"""
        with aioresponses() as m:
            # Мокируем POST запрос к API аутентификации с ошибкой
            m.post(
                "http://localhost:8080/api/login",
                status=401
            )
            
            await bot.handle_login(message, "test_user", "wrong_password")
            
            # Проверяем, что токен не сохранен
            assert 12345 not in bot.user_tokens
            
            # Проверяем, что отправлено сообщение об ошибке
            message.answer.assert_called_once_with("❌ Неверное имя пользователя или пароль")
    
    @pytest.mark.asyncio
    async def test_handle_login_exception(self, bot, message):
        """Тест обработки исключения при аутентификации"""
        with aioresponses() as m:
            # Мокируем POST запрос к API аутентификации с исключением
            m.post(
                "http://localhost:8080/api/login",
                exception=Exception("Connection error")
            )
            
            await bot.handle_login(message, "test_user", "test_password")
            
            # Проверяем, что токен не сохранен
            assert 12345 not in bot.user_tokens
            
            # Проверяем, что отправлено сообщение об ошибке
            message.answer.assert_called_once_with("❌ Ошибка соединения с сервером")
    
    @pytest.mark.asyncio
    async def test_handle_list_subscriptions_success(self, bot, message):
        """Тест успешного получения списка подписок"""
        # Устанавливаем токен
        bot.user_tokens[12345] = "test_token"
        
        # Создаем mock данных подписок
        subscriptions_data = [
            {
                "id": "sub1",
                "user_id": "12345",
                "name": "Netflix",
                "price": "15.99",
                "currency": "USD",
                "billing_cycle": "monthly",
                "next_payment_date": "2023-12-31",
                "is_active": True
            },
            {
                "id": "sub2",
                "user_id": "12345",
                "name": "Spotify",
                "price": "9.99",
                "currency": "USD",
                "billing_cycle": "monthly",
                "next_payment_date": "2023-12-15",
                "is_active": True
            }
        ]
        
        with aioresponses() as m:
            # Мокируем GET запрос к API для получения списка подписок
            m.get(
                "http://localhost:8080/api/subscriptions",
                payload=subscriptions_data,
                status=200
            )
            
            await bot.handle_list_command(message)
            
            # Проверяем, что отправлено сообщение со списком подписок
            message.answer.assert_called_once()
            args, kwargs = message.answer.call_args
            message_text = args[0]
            assert "📋 Ваши подписки:" in message_text
            assert "Netflix" in message_text
            assert "Spotify" in message_text
    
    @pytest.mark.asyncio
    async def test_handle_list_subscriptions_empty(self, bot, message):
        """Тест получения пустого списка подписок"""
        # Устанавливаем токен
        bot.user_tokens[12345] = "test_token"
        
        with aioresponses() as m:
            # Мокируем GET запрос к API для получения пустого списка подписок
            m.get(
                "http://localhost:8080/api/subscriptions",
                payload=[],
                status=200
            )
            
            await bot.handle_list_command(message)
            
            # Проверяем, что отправлено сообщение об отсутствии подписок
            message.answer.assert_called_once_with("📋 У вас пока нет подписок.\nДобавьте первую: /add")
    
    @pytest.mark.asyncio
    async def test_handle_list_subscriptions_failure(self, bot, message):
        """Тест неудачного получения списка подписок"""
        # Устанавливаем токен
        bot.user_tokens[12345] = "test_token"
        
        with aioresponses() as m:
            # Мокируем GET запрос к API для получения списка подписок с ошибкой
            m.get(
                "http://localhost:8080/api/subscriptions",
                status=500
            )
            
            await bot.handle_list_command(message)
            
            # Проверяем, что отправлено сообщение об ошибке
            message.answer.assert_called_once_with("❌ Ошибка при получении списка подписок")
    
    @pytest.mark.asyncio
    async def test_create_subscription_success(self, bot, message):
        """Тест успешного создания подписки"""
        # Устанавливаем токен
        bot.user_tokens[12345] = "test_token"
        
        # Данные подписки
        subscription_data = {
            "name": "Test Subscription",
            "price": "9.99",
            "currency": "USD",
            "billing_cycle": "monthly",
            "next_payment_date": "2023-12-31"
        }
        
        with aioresponses() as m:
            # Мокируем POST запрос к API для создания подписки
            m.post(
                "http://localhost:8080/api/subscriptions",
                status=201
            )
            
            await bot.create_subscription(message, subscription_data)
            
            # Проверяем, что отправлено сообщение об успешном создании
            message.answer.assert_called_once_with("✅ Подписка успешно создана!")
    
    @pytest.mark.asyncio
    async def test_create_subscription_failure(self, bot, message):
        """Тест неудачного создания подписки"""
        # Устанавливаем токен
        bot.user_tokens[12345] = "test_token"
        
        # Данные подписки
        subscription_data = {
            "name": "Test Subscription",
            "price": "9.99",
            "currency": "USD",
            "billing_cycle": "monthly",
            "next_payment_date": "2023-12-31"
        }
        
        with aioresponses() as m:
            # Мокируем POST запрос к API для создания подписки с ошибкой
            m.post(
                "http://localhost:8080/api/subscriptions",
                status=400
            )
            
            await bot.create_subscription(message, subscription_data)
            
            # Проверяем, что отправлено сообщение об ошибке
            message.answer.assert_called_once_with("❌ Ошибка при создании подписки")
    
    @pytest.mark.asyncio
    async def test_handle_delete_subscription_success(self, bot, message):
        """Тест успешного удаления подписки"""
        from aiogram.filters import CommandObject
        
        # Устанавливаем токен
        bot.user_tokens[12345] = "test_token"
        
        # Создаем mock команды
        command = Mock(spec=CommandObject)
        command.args = "test_subscription_id"
        
        with aioresponses() as m:
            # Мокируем DELETE запрос к API для удаления подписки
            m.delete(
                "http://localhost:8080/api/subscriptions/test_subscription_id",
                status=200
            )
            
            await bot.handle_delete_command(message, command)
            
            # Проверяем, что отправлено сообщение об успешном удалении
            message.answer.assert_called_once_with("✅ Подписка успешно удалена!")
    
    @pytest.mark.asyncio
    async def test_handle_stats_command_success(self, bot, message):
        """Тест успешного получения статистики"""
        # Устанавливаем токен
        bot.user_tokens[12345] = "test_token"
        
        # Создаем mock данных подписок
        subscriptions_data = [
            {
                "id": "sub1",
                "user_id": "12345",
                "name": "Netflix",
                "price": "15.99",
                "currency": "USD",
                "billing_cycle": "monthly",
                "next_payment_date": "2023-12-31",
                "is_active": True
            },
            {
                "id": "sub2",
                "user_id": "12345",
                "name": "Spotify",
                "price": "9.99",
                "currency": "USD",
                "billing_cycle": "monthly",
                "next_payment_date": "2023-12-15",
                "is_active": True
            }
        ]
        
        with aioresponses() as m:
            # Мокируем GET запрос к API для получения списка подписок
            m.get(
                "http://localhost:8080/api/subscriptions",
                payload=subscriptions_data,
                status=200
            )
            
            await bot.handle_stats_command(message)
            
            # Проверяем, что отправлено сообщение со статистикой
            message.answer.assert_called_once()
            args, kwargs = message.answer.call_args
            message_text = args[0]
            assert "📊 Статистика ваших подписок:" in message_text
            assert "Всего подписок: 2" in message_text
            assert "Общие расходы в месяц: $25.98" in message_text

if __name__ == "__main__":
    pytest.main([__file__, "-v"])