#!/usr/bin/env python3
"""
Скрипт для запуска Telegram бота SubTracker
"""

import asyncio
import os
import sys
from dotenv import load_dotenv

# Добавляем путь к модулю бота
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

def check_environment():
    """Проверка наличия необходимых переменных окружения"""
    load_dotenv()
    
    bot_token = os.getenv("TELEGRAM_BOT_TOKEN")
    if not bot_token:
        print("❌ Не найден TELEGRAM_BOT_TOKEN в переменных окружения")
        print("Пожалуйста, создайте файл .env с переменной TELEGRAM_BOT_TOKEN")
        return False
    
    return True

def check_dependencies():
    """Проверка наличия необходимых зависимостей"""
    try:
        import aiogram
        import aiohttp
        import dotenv
        print(f"✅ Все зависимости установлены")
        return True
    except ImportError as e:
        print(f"❌ Отсутствует зависимость: {e}")
        print("Пожалуйста, установите зависимости командой: pip install -r requirements.txt")
        return False

async def main():
    """Главная функция для запуска бота"""
    print("🚀 Запуск Telegram бота SubTracker")
    
    # Проверка окружения
    if not check_environment():
        return
    
    # Проверка зависимостей
    if not check_dependencies():
        return
    
    try:
        # Импортируем и запускаем бота
        from bot import SubTrackerBot
        
        print("🔄 Инициализация бота...")
        bot = SubTrackerBot()
        
        print("✅ Бот успешно инициализирован")
        print("📡 Подключение к Telegram API...")
        print("💡 Бот запущен! Нажмите Ctrl+C для остановки")
        
        # Запуск бота
        await bot.start()
        
    except KeyboardInterrupt:
        print("\n👋 Бот остановлен пользователем")
    except Exception as e:
        print(f"❌ Ошибка при запуске бота: {e}")
        import traceback
        traceback.print_exc()

if __name__ == "__main__":
    asyncio.run(main())