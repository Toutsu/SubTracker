// Утилиты для форматирования данных

// Функция для перевода цикла оплаты
export const translateBillingCycle = (cycle: string): string => {
  switch (cycle) {
    case 'monthly':
      return 'Ежемесячно';
    case 'yearly':
      return 'Ежегодно';
    case 'weekly':
      return 'Еженедельно';
    default:
      return cycle;
  }
};

// Функция для форматирования даты
export const formatDate = (dateString: string): string => {
  // В реальном приложении здесь будет более сложное форматирование
  // Например, с использованием библиотеки date-fns или moment.js
  try {
    const date = new Date(dateString);
    return date.toLocaleDateString('ru-RU');
  } catch (error) {
    // Если не удалось распарсить дату, возвращаем исходную строку
    return dateString;
  }
};

// Функция для форматирования цены
export const formatPrice = (price: number, currency: string): string => {
  // В реальном приложении здесь будет форматирование с учетом локали и валюты
  return `${price.toFixed(2)} ${currency}`;
};