import { useState, useEffect } from "react";
import { Button } from "./ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "./ui/card";
import { Plus, LogOut, TrendingUp, Calendar, CreditCard } from "lucide-react";
import { SubscriptionModal } from "./SubscriptionModal";
import { SubscriptionCard } from "./SubscriptionCard";
import { subscriptionApi, SubscriptionResponse, CreateSubscriptionRequest } from "../services/api";
import { convertToRubles } from "../services/currencyService";
import { type Subscription } from "./SubscriptionModal";

interface User {
  name: string;
  email: string;
}

interface DashboardProps {
  user: User;
  onLogout: () => void;
}

export function Dashboard({ user, onLogout }: DashboardProps) {
  const [subscriptions, setSubscriptions] = useState<Subscription[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [monthlyTotal, setMonthlyTotal] = useState<number>(0);
  const [yearlyTotal, setYearlyTotal] = useState<number>(0);

  // Загрузка подписок с сервера
  useEffect(() => {
    loadSubscriptions();
  }, []);

  const loadSubscriptions = async () => {
    try {
      setLoading(true);
      // Получаем userId из данных пользователя
      const userId = user.email; // Используем email как userId
      const response = await subscriptionApi.getSubscriptionsByUserId({ userId });
      // Преобразуем SubscriptionResponse в Subscription
      const mappedSubscriptions: Subscription[] = response.map(sub => ({
        id: sub.id,
        name: sub.name,
        price: parseFloat(sub.price),
        currency: sub.currency,
        billingPeriod: sub.billing_period.toLowerCase(),
        nextPayment: sub.next_payment,
        category: sub.category,
        description: sub.description || "",
        isActive: sub.is_active
      }));
      setSubscriptions(mappedSubscriptions);
      
      // Пересчитываем_totals после загрузки подписок
      recalculateTotals(mappedSubscriptions);
    } catch (err: any) {
      // Проверяем, есть ли у ошибки сообщение от сервера
      if (err && err.response) {
        try {
          const errorData = await err.response.json();
          setError(errorData.message || "Ошибка загрузки подписок");
        } catch (parseError) {
          setError("Ошибка загрузки подписок");
        }
      } else {
        setError("Ошибка подключения к серверу");
        console.error("Error loading subscriptions:", err);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleAddSubscription = async (newSubscription: Omit<Subscription, 'id'>) => {
    try {
      // Преобразуем Subscription в CreateSubscriptionRequest
      const createRequest: CreateSubscriptionRequest = {
        user_id: user.email, // Используем email как userId
        name: newSubscription.name,
        price: newSubscription.price.toString(),
        currency: newSubscription.currency,
        billing_period: newSubscription.billingPeriod.toUpperCase(),
        next_payment: newSubscription.nextPayment,
        category: newSubscription.category,
        description: newSubscription.description
      };
      
      // Отправляем запрос к API
      const response = await subscriptionApi.createSubscription({ createSubscriptionRequest: createRequest });
      
      // Преобразуем SubscriptionResponse в Subscription
      const createdSubscription: Subscription = {
        id: response.id,
        name: response.name,
        price: parseFloat(response.price),
        currency: response.currency,
        billingPeriod: response.billing_period.toLowerCase(),
        nextPayment: response.next_payment,
        category: response.category,
        description: response.description || "",
        isActive: response.is_active
      };
      
      // Обновляем состояние
      setSubscriptions(prev => [...prev, createdSubscription]);
      
      // Пересчитываем_totals
      recalculateTotals([...subscriptions, createdSubscription]);
    } catch (err: any) {
      // Проверяем, есть ли у ошибки сообщение от сервера
      if (err && err.response) {
        try {
          const errorData = await err.response.json();
          setError(errorData.message || "Ошибка создания подписки");
        } catch (parseError) {
          setError("Ошибка создания подписки");
        }
      } else {
        setError("Ошибка подключения к серверу");
        console.error("Error creating subscription:", err);
      }
    }
  };

  const handleDeleteSubscription = async (id: string) => {
    try {
      // Отправляем запрос к API для удаления подписки
      await subscriptionApi.deleteSubscription({ id });
      
      // Обновляем состояние, удаляя подписку локально
      setSubscriptions(prev => prev.filter(sub => sub.id !== id));
      
      // Пересчитываем_totals
      recalculateTotals(subscriptions.filter(sub => sub.id !== id));
    } catch (err: any) {
      // Проверяем, есть ли у ошибки сообщение от сервера
      if (err && err.response) {
        try {
          const errorData = await err.response.json();
          setError(errorData.message || "Ошибка удаления подписки");
        } catch (parseError) {
          setError("Ошибка удаления подписки");
        }
      } else {
        setError("Ошибка подключения к серверу");
        console.error("Error deleting subscription:", err);
      }
    }
  };

  // Функция для пересчета_totals
  const recalculateTotals = async (subs: Subscription[]) => {
    try {
      let monthlyTotal = 0;
      let yearlyTotal = 0;
      
      for (const sub of subs) {
        // Конвертируем сумму подписки в рубли
        const amountInRubles = await convertToRubles(sub.price, sub.currency);
        
        // Рассчитываем месячную и годовую сумму в рублях
        const monthlyAmount = sub.billingPeriod === "yearly" ? amountInRubles / 12 : amountInRubles;
        const yearlyAmount = sub.billingPeriod === "monthly" ? amountInRubles * 12 : amountInRubles;
        
        monthlyTotal += monthlyAmount;
        yearlyTotal += yearlyAmount;
      }
      
      setMonthlyTotal(monthlyTotal);
      setYearlyTotal(yearlyTotal);
    } catch (err) {
      console.error("Ошибка при пересчете_totals:", err);
      setError("Ошибка при расчете_total сумм");
    }
  };

  const getUpcomingPayments = () => {
    return subscriptions
      .sort((a, b) => new Date(a.nextPayment).getTime() - new Date(b.nextPayment).getTime())
      .slice(0, 3);
  };

  return (
    <div className="min-h-screen bg-muted/30">
      {/* Header */}
      <header className="border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-2xl">Мои подписки</h1>
              <p className="text-muted-foreground">Добро пожаловать, {user.name}</p>
            </div>
            <div className="flex items-center gap-4">
              <Button onClick={() => setIsModalOpen(true)}>
                <Plus className="h-4 w-4 mr-2" />
                Добавить подписку
              </Button>
              <Button variant="ghost" onClick={onLogout}>
                <LogOut className="h-4 w-4 mr-2" />
                Выйти
              </Button>
            </div>
          </div>
        </div>
      </header>

      <main className="container mx-auto px-4 py-8">
        {/* Statistics Cards */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm">Всего подписок</CardTitle>
              <CreditCard className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl">{subscriptions.length}</div>
              <p className="text-xs text-muted-foreground">
                активных сервисов
              </p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm">Ежемесячно</CardTitle>
              <TrendingUp className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl">{Math.round(monthlyTotal)} ₽</div>
              <p className="text-xs text-muted-foreground">
                средний расход в месяц
              </p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm">Ежегодно</CardTitle>
              <Calendar className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl">{Math.round(yearlyTotal)} ₽</div>
              <p className="text-xs text-muted-foreground">
                общий расход в год
              </p>
            </CardContent>
          </Card>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Subscriptions List */}
          <div className="lg:col-span-2">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-xl">Ваши подписки</h2>
            </div>
            
            {loading ? (
              <div className="flex justify-center items-center h-32">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
              </div>
            ) : error ? (
              <Card className="p-8 text-center">
                <div className="text-destructive">
                  <p>{error}</p>
                  <Button onClick={loadSubscriptions} className="mt-4">
                    Повторить попытку
                  </Button>
                </div>
              </Card>
            ) : subscriptions.length === 0 ? (
              <Card className="p-8 text-center">
                <div className="text-muted-foreground">
                  <CreditCard className="h-12 w-12 mx-auto mb-4 opacity-50" />
                  <p>У вас пока нет подписок</p>
                  <Button onClick={() => setIsModalOpen(true)} className="mt-4">
                    Добавить первую подписку
                  </Button>
                </div>
              </Card>
            ) : (
              <div className="grid gap-4">
                {subscriptions.map((subscription) => (
                  <SubscriptionCard
                    key={subscription.id}
                    subscription={subscription}
                    onDelete={handleDeleteSubscription}
                  />
                ))}
              </div>
            )}
          </div>

          {/* Upcoming Payments Sidebar */}
          <div>
            <Card>
              <CardHeader>
                <CardTitle>Ближайшие платежи</CardTitle>
                <CardDescription>
                  Следующие списания с карты
                </CardDescription>
              </CardHeader>
              <CardContent>
                {getUpcomingPayments().length === 0 ? (
                  <p className="text-muted-foreground text-sm">
                    Нет предстоящих платежей
                  </p>
                ) : (
                  <div className="space-y-4">
                    {getUpcomingPayments().map((subscription) => (
                      <div key={subscription.id} className="flex items-center justify-between">
                        <div>
                          <p className="text-sm">{subscription.name}</p>
                          <p className="text-xs text-muted-foreground">
                            {new Date(subscription.nextPayment).toLocaleDateString('ru-RU')}
                          </p>
                        </div>
                        <div className="text-sm">
                          {subscription.price} {subscription.currency}
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          </div>
        </div>
      </main>

      <SubscriptionModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onAddSubscription={handleAddSubscription}
      />
    </div>
  );
}