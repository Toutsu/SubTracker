import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "./ui/card";
import { Badge } from "./ui/badge";
import { Button } from "./ui/button";
import { Trash2, Calendar, CreditCard } from "lucide-react";
import { Subscription } from "./SubscriptionModal";

interface SubscriptionCardProps {
  subscription: Subscription;
  onDelete: (id: string) => void;
}

export function SubscriptionCard({ subscription, onDelete }: SubscriptionCardProps) {
  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('ru-RU');
  };

  const getCategoryColor = (category: string) => {
    const categoryMap: { [key: string]: string } = {
      "Развлечения": "entertainment",
      "Продуктивность": "productivity",
      "Дизайн": "design",
      "Облачные сервисы": "cloud",
      "Музыка": "music",
      "Видео": "video",
      "Другое": "other"
    };
    
    const colors: { [key: string]: string } = {
      entertainment: "bg-purple-100 text-purple-800",
      productivity: "bg-blue-100 text-blue-800",
      design: "bg-pink-100 text-pink-800",
      cloud: "bg-green-100 text-green-800",
      music: "bg-orange-100 text-orange-800",
      video: "bg-red-100 text-red-800",
      other: "bg-gray-100 text-gray-800",
    };
    
    const englishCategory = categoryMap[category] || "other";
    return colors[englishCategory] || colors.other;
  };

  const getBillingPeriodText = (period: string) => {
    const lowerPeriod = period.toLowerCase();
    return lowerPeriod === "monthly" || lowerPeriod === "месячный" ? "мес." : "год";
  };

  return (
    <Card className="relative">
      <CardHeader className="pb-3">
        <div className="flex items-start justify-between">
          <div>
            <CardTitle className="text-lg">{subscription.name}</CardTitle>
            <CardDescription>
              <Badge variant="secondary" className={getCategoryColor(subscription.category)}>
                {subscription.category}
              </Badge>
              {!subscription.isActive && (
                <Badge variant="destructive" className="ml-2">
                  Неактивна
                </Badge>
              )}
            </CardDescription>
          </div>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => onDelete(subscription.id)}
            className="text-muted-foreground hover:text-destructive"
          >
            <Trash2 className="h-4 w-4" />
          </Button>
        </div>
      </CardHeader>
      <CardContent className="pt-0">
        <div className="space-y-3">
          <div className="flex items-center gap-2">
            <CreditCard className="h-4 w-4 text-muted-foreground" />
            <span className="text-lg">
              {subscription.price} {subscription.currency}
              <span className="text-sm text-muted-foreground ml-1">
                / {getBillingPeriodText(subscription.billingPeriod)}
              </span>
            </span>
          </div>
          
          <div className="flex items-center gap-2">
            <Calendar className="h-4 w-4 text-muted-foreground" />
            <span className="text-sm text-muted-foreground">
              Следующий платеж: {formatDate(subscription.nextPayment)}
            </span>
          </div>

          {subscription.description && (
            <p className="text-sm text-muted-foreground mt-2">
              {subscription.description}
            </p>
          )}
        </div>
      </CardContent>
    </Card>
  );
}