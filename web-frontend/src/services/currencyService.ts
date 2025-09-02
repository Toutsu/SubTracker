import axios from 'axios';

// Тип для курсов валют
interface CurrencyRates {
  [currency: string]: number;
}

// Кэш для хранения курсов валют
let currencyRatesCache: CurrencyRates | null = null;
let lastFetchTime: number | null = null;
const CACHE_DURATION = 1000 * 60; // 1 час

/**
 * Получает курсы валют с API
 * @returns Объект с курсами валют
 */
export const fetchCurrencyRates = async (): Promise<CurrencyRates> => {
  const now = Date.now();
  
  // Проверяем, есть ли закэшированные данные и не истекло ли время кэша
  if (currencyRatesCache && lastFetchTime && (now - lastFetchTime < CACHE_DURATION)) {
    return currencyRatesCache;
  }
  
  try {
    // Используем API с RUB как базовую валюту
    const response = await axios.get('https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/rub.json');
    const rates = response.data.rub;
    
    // Сохраняем в кэш
    currencyRatesCache = rates;
    lastFetchTime = now;
    
    return rates;
  } catch (error) {
    console.error('Ошибка при получении курсов валют:', error);
    throw new Error('Не удалось получить курсы валют');
  }
};

/**
 * Конвертирует сумму из указанной валюты в рубли
 * @param amount Сумма для конвертации
 * @param fromCurrency Исходная валюта
 * @returns Сумма в рублях
 */
export const convertToRubles = async (amount: number, fromCurrency: string): Promise<number> => {
  // Сопоставление символов валют с их кодами в ISO
  const currencyMap: { [key: string]: string } = {
    '₽': 'rub',
    '$': 'usd',
    '€': 'eur',
    '£': 'gbp',
    '¥': 'jpy',
    '₣': 'chf',
    '₹': 'inr',
    '₩': 'krw',
    '₴': 'uah',
    '₸': 'kzt'
  };

  // Если валюта уже в рублях, возвращаем исходную сумму
  if (fromCurrency.toLowerCase() === 'rub' || fromCurrency === '₽') {
    return amount;
  }
  
  try {
    const rates = await fetchCurrencyRates();
    // Получаем код валюты в ISO
    const currencyCode = currencyMap[fromCurrency] || fromCurrency.toLowerCase();
    
    // Проверяем, существует ли курс для запрашиваемой валюты
    if (rates[currencyCode] === undefined) {
      throw new Error(`Курс для валюты ${fromCurrency} не найден`);
    }
    
    // Конвертируем сумму в рубли
    // API возвращает сколько единиц валюты за 1 рубль, поэтому для конвертации в рубли нужно делить
    const amountInRub = amount / rates[currencyCode];
    return amountInRub;
  } catch (error) {
    console.error(`Ошибка при конвертации ${amount} ${fromCurrency} в рубли:`, error);
    throw error;
  }
};