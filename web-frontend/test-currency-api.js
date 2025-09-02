const axios = require('axios');

async function testCurrencyApi() {
  try {
    const response = await axios.get('https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/rub.json');
    console.log('Данные API:');
    console.log(JSON.stringify(response.data, null, 2));
    
    // Проверим конкретные курсы
    const rates = response.data.rub;
    console.log('\nКурсы для USD, EUR и других валют:');
    console.log('USD:', rates.usd);
    console.log('EUR:', rates.eur);
    console.log('GBP:', rates.gbp);
    console.log('JPY:', rates.jpy);
  } catch (error) {
    console.error('Ошибка при получении данных:', error.message);
 }
}

testCurrencyApi();