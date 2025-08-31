// Типы для подписок, основанные на shared/src/commonMain/Subscription.kt
export type BillingCycle = 'monthly' | 'yearly' | 'weekly';

export interface Subscription {
  id: string;
  userId: string;
  name: string;
  price: number; // В Kotlin это Double, в TypeScript number
  currency: string;
  billingCycle: BillingCycle;
  nextPaymentDate: string; // ISO format date string
  isActive: boolean;
}

export interface CreateSubscriptionRequest {
  userId: string;
  name: string;
  price: number; // В Kotlin это String, но логичнее использовать number
  currency: string;
  billingCycle: BillingCycle;
  nextPaymentDate: string; // ISO format date string
}