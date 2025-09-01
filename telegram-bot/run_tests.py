#!/usr/bin/env python3
"""
Скрипт для запуска тестов Telegram бота SubTracker
"""

import subprocess
import sys
import os

def run_tests():
    """Запуск тестов с помощью pytest"""
    try:
        # Установка переменных окружения для тестов
        os.environ['TELEGRAM_BOT_TOKEN'] = '123456789:ABCdefGhIJKlmNoPQRsTUVwxyZ'
        
        # Запуск pytest
        result = subprocess.run([
            sys.executable, '-m', 'pytest',
            'tests/',
            '-v',
            '--tb=short'
        ], cwd=os.path.dirname(os.path.abspath(__file__)))
        
        return result.returncode == 0
    except Exception as e:
        print(f"Ошибка при запуске тестов: {e}")
        return False

if __name__ == "__main__":
    success = run_tests()
    sys.exit(0 if success else 1)