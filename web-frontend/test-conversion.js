// Тестовый скрипт для проверки конвертации валют
const axios = require('axios');

// Сопоставление символов валют с их кодами в ISO
const currencyMap = {
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

async function fetchCurrencyRates() {
  try {
    // Используем API с RUB как базовую валюту
    const response = await axios.get('https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/rub.json');
    const rates = response.data.rub;
    return rates;
  } catch (error) {
    console.error('Ошибка при получении курсов валют:', error);
    throw new Error('Не удалось получить курсы валют');
  }
}

async function convertToRubles(amount, fromCurrency) {
  // Если валюта уже в рублях, возвращаем исходную сумму
  if (fromCurrency.toLowerCase() === 'rub' || fromCurrency === '₽') {
    console.log(`Конвертация: ${amount} ${fromCurrency} = ${amount} ₽ (валюта уже в рублях)`);
    return amount;
  }
  
  try {
    const rates = await fetchCurrencyRates();
    // Получаем код валюты в ISO
    const currencyCode = currencyMap[fromCurrency] || fromCurrency.toLowerCase();
    
    console.log(`Конвертация: ${amount} ${fromCurrency} (код: ${currencyCode})`);
    
    // Проверяем, существует ли курс для запрашиваемой валюты
    if (rates[currencyCode] === undefined) {
      throw new Error(`Курс для валюты ${fromCurrency} не найден`);
    }
    
    console.log(`Курс для ${currencyCode}: ${rates[currencyCode]}`);
    
    // Конвертируем сумму в рубли
    // API возвращает сколько единиц валюты за 1 рубль, поэтому для конвертации в рубли нужно делить
    const amountInRub = amount / rates[currencyCode];
    console.log(`Результат конвертации: ${amount} ${fromCurrency} = ${amountInRub} ₽`);
    return amountInRub;
  } catch (error) {
    console.error(`Ошибка при конвертации ${amount} ${fromCurrency} в рубли:`, error);
    throw error;
  }
}

// Тестовые примеры
async function runTests() {
  console.log('=== Тест конвертации валют ===\n');
  
  try {
    // Тест 1: 500 долларов США в рубли
    console.log('Тест 1: 500 долларов США в рубли');
    const result1 = await convertToRubles(500, '$');
    console.log(`Результат: 500 $ = ${result1.toFixed(2)} ₽\n`);
    
    // Тест 2: 490 рублей в рубли (должно остаться 490)
    console.log('Тест 2: 490 рублей в рубли');
    const result2 = await convertToRubles(490, '₽');
    console.log(`Результат: 490 ₽ = ${result2.toFixed(2)} ₽\n`);
    
    // Тест 3: 500 евро в рубли
    console.log('Тест 3: 500 евро в рубли');
    const result3 = await convertToRubles(500, '€');
    console.log(`Результат: 500 € = ${result3.toFixed(2)} ₽\n`);
    
  } catch (error) {
    console.error('Ошибка при выполнении тестов:', error);
  }
}

runTests();