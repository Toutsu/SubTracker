import { useState } from "react";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "./ui/dialog";
import { Button } from "./ui/button";
import { Input } from "./ui/input";
import { Label } from "./ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "./ui/select";
import { Textarea } from "./ui/textarea";

export interface Subscription {
  id: string;
  name: string;
  price: number;
  currency: string;
  billingPeriod: string;
  nextPayment: string;
  category: string;
  description?: string;
  isActive: boolean;
}

interface SubscriptionModalProps {
  isOpen: boolean;
  onClose: () => void;
  onAddSubscription: (subscription: Omit<Subscription, 'id'>) => void;
}

export function SubscriptionModal({ isOpen, onClose, onAddSubscription }: SubscriptionModalProps) {
  const [name, setName] = useState("");
  const [price, setPrice] = useState("");
  const [currency, setCurrency] = useState("₽");
  const [billingPeriod, setBillingPeriod] = useState<"monthly" | "yearly">("monthly");
  const [nextPayment, setNextPayment] = useState("");
  const [category, setCategory] = useState("");
  const [description, setDescription] = useState("");

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    onAddSubscription({
      name,
      price: parseFloat(price),
      currency,
      billingPeriod,
      nextPayment,
      category,
      description: description || undefined,
      isActive: true, // Новые подписки активны по умолчанию
    });

    // Reset form
    setName("");
    setPrice("");
    setCurrency("₽");
    setBillingPeriod("monthly");
    setNextPayment("");
    setCategory("");
    setDescription("");
    
    onClose();
  };

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>Добавить подписку</DialogTitle>
          <DialogDescription>
            Заполните информацию о новой подписке
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="name">Название сервиса</Label>
            <Input
              id="name"
              placeholder="Netflix, Spotify, Adobe..."
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
          </div>
          
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="price">Стоимость</Label>
              <Input
                id="price"
                type="number"
                step="0.01"
                placeholder="999"
                value={price}
                onChange={(e) => setPrice(e.target.value)}
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="currency">Валюта</Label>
              <Select value={currency} onValueChange={setCurrency}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="₽">₽ Рубль</SelectItem>
                  <SelectItem value="$">$ Доллар</SelectItem>
                  <SelectItem value="€">€ Евро</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="billingPeriod">Период оплаты</Label>
            <Select value={billingPeriod} onValueChange={(value: "monthly" | "yearly") => setBillingPeriod(value)}>
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="monthly">Ежемесячно</SelectItem>
                <SelectItem value="yearly">Ежегодно</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label htmlFor="nextPayment">Дата следующего платежа</Label>
            <Input
              id="nextPayment"
              type="date"
              value={nextPayment}
              onChange={(e) => setNextPayment(e.target.value)}
              required
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="category">Категория</Label>
            <Select value={category} onValueChange={setCategory}>
              <SelectTrigger>
                <SelectValue placeholder="Выберите категорию" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="Развлечения">Развлечения</SelectItem>
                <SelectItem value="Продуктивность">Продуктивность</SelectItem>
                <SelectItem value="Дизайн">Дизайн</SelectItem>
                <SelectItem value="Облачные сервисы">Облачные сервисы</SelectItem>
                <SelectItem value="Музыка">Музыка</SelectItem>
                <SelectItem value="Видео">Видео</SelectItem>
                <SelectItem value="Другое">Другое</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <Label htmlFor="description">Описание (опционально)</Label>
            <Textarea
              id="description"
              placeholder="Дополнительная информация о подписке..."
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
          </div>

          <div className="flex gap-2 pt-4">
            <Button type="button" variant="outline" className="flex-1" onClick={onClose}>
              Отмена
            </Button>
            <Button type="submit" className="flex-1">
              Добавить
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
}