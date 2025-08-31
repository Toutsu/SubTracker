import { renderHook, act } from '@testing-library/react';
import { useSubscriptions } from '../../hooks/useSubscriptions';
import { subscriptionService } from '../../services/subscriptions';

// Mock the subscriptionService
jest.mock('../../services/subscriptions', () => ({
  subscriptionService: {
    getAllSubscriptions: jest.fn(),
    addSubscription: jest.fn(),
    deleteSubscription: jest.fn(),
  },
}));

describe('useSubscriptions', () => {
  const mockSubscriptionService = subscriptionService as jest.Mocked<typeof subscriptionService>;
  const userId = 'user123';

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('loadSubscriptions', () => {
    it('should successfully load subscriptions', async () => {
      const subscriptions: any[] = [
        {
          id: 'sub1',
          userId: 'user123',
          name: 'Netflix',
          price: 15.99,
          currency: 'USD',
          billingCycle: 'monthly' as const,
          nextPaymentDate: '2023-12-01',
          isActive: true,
        },
      ];

      mockSubscriptionService.getAllSubscriptions.mockResolvedValueOnce(subscriptions);

      const { result } = renderHook(() => useSubscriptions(userId));

      // Wait for useEffect to complete
      await act(async () => {
        await new Promise(resolve => setTimeout(resolve, 0));
      });

      expect(result.current.subscriptions).toEqual(subscriptions);
      expect(result.current.loading).toBe(false);
      expect(result.current.error).toBeNull();
      expect(mockSubscriptionService.getAllSubscriptions).toHaveBeenCalledWith(userId);
    });

    it('should set error when loading subscriptions fails', async () => {
      const error = new Error('Failed to load subscriptions');
      mockSubscriptionService.getAllSubscriptions.mockRejectedValueOnce(error);

      const { result } = renderHook(() => useSubscriptions(userId));

      // Wait for useEffect to complete
      await act(async () => {
        await new Promise(resolve => setTimeout(resolve, 0));
      });

      expect(result.current.subscriptions).toEqual([]);
      expect(result.current.loading).toBe(false);
      expect(result.current.error).toBe('Не удалось загрузить подписки');
    });
  });

  describe('addSubscription', () => {
    it('should successfully add a subscription', async () => {
      const newSubscription: any = {
        id: 'sub2',
        userId: 'user123',
        name: 'Spotify',
        price: 9.99,
        currency: 'USD',
        billingCycle: 'monthly' as const,
        nextPaymentDate: '2023-12-15',
        isActive: true,
      };

      mockSubscriptionService.addSubscription.mockResolvedValueOnce(newSubscription);

      const { result } = renderHook(() => useSubscriptions(userId));

      let addedSubscription;
      await act(async () => {
        addedSubscription = await result.current.addSubscription({
          userId,
          name: 'Spotify',
          price: 9.99,
          currency: 'USD',
          billingCycle: 'monthly',
          nextPaymentDate: '2023-12-15',
        });
      });

      expect(addedSubscription).toEqual(newSubscription);
      expect(result.current.subscriptions).toContainEqual(newSubscription);
      expect(mockSubscriptionService.addSubscription).toHaveBeenCalled();
    });

    it('should set error when adding subscription fails', async () => {
      const error = new Error('Failed to add subscription');
      mockSubscriptionService.addSubscription.mockRejectedValueOnce(error);

      const { result } = renderHook(() => useSubscriptions(userId));

      await expect(act(async () => {
        await result.current.addSubscription({
          userId,
          name: 'Spotify',
          price: 9.99,
          currency: 'USD',
          billingCycle: 'monthly',
          nextPaymentDate: '2023-12-15',
        });
      })).rejects.toThrow('Failed to add subscription');

      expect(result.current.error).toBe('Не удалось добавить подписку');
    });
  });

  describe('deleteSubscription', () => {
    it('should successfully delete a subscription', async () => {
      const subscriptions: any[] = [
        {
          id: 'sub1',
          userId: 'user123',
          name: 'Netflix',
          price: 15.99,
          currency: 'USD',
          billingCycle: 'monthly' as const,
          nextPaymentDate: '2023-12-01',
          isActive: true,
        },
        {
          id: 'sub2',
          userId: 'user123',
          name: 'Spotify',
          price: 9.99,
          currency: 'USD',
          billingCycle: 'monthly' as const,
          nextPaymentDate: '2023-12-15',
          isActive: true,
        },
      ];

      // First, load subscriptions
      mockSubscriptionService.getAllSubscriptions.mockResolvedValueOnce(subscriptions);
      
      // Then mock delete
      mockSubscriptionService.deleteSubscription.mockResolvedValueOnce(undefined);

      const { result } = renderHook(() => useSubscriptions(userId));

      // Wait for initial load
      await act(async () => {
        await new Promise(resolve => setTimeout(resolve, 0));
      });

      // Delete a subscription
      await act(async () => {
        await result.current.deleteSubscription('sub1');
      });

      expect(result.current.subscriptions).toEqual([subscriptions[1]]);
      expect(mockSubscriptionService.deleteSubscription).toHaveBeenCalledWith('sub1');
    });

    it('should set error when deleting subscription fails', async () => {
      const error = new Error('Failed to delete subscription');
      mockSubscriptionService.deleteSubscription.mockRejectedValueOnce(error);

      const { result } = renderHook(() => useSubscriptions(userId));

      await expect(act(async () => {
        await result.current.deleteSubscription('sub1');
      })).rejects.toThrow('Failed to delete subscription');

      expect(result.current.error).toBe('Не удалось удалить подписку');
    });
  });
});