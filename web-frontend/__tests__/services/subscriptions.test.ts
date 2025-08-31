import { SubscriptionService } from '../../services/subscriptions';
import { apiClient } from '../../services/api';
import type { Subscription, CreateSubscriptionRequest } from '../../types/subscription';

// Mock the apiClient
jest.mock('../../services/api', () => ({
  apiClient: {
    get: jest.fn(),
    post: jest.fn(),
    delete: jest.fn(),
  },
}));

describe('SubscriptionService', () => {
  let subscriptionService: SubscriptionService;
  const mockApiClient = apiClient as jest.Mocked<typeof apiClient>;

  beforeEach(() => {
    subscriptionService = new SubscriptionService();
    jest.clearAllMocks();
  });

  describe('getAllSubscriptions', () => {
    it('should successfully fetch all subscriptions for a user', async () => {
      const userId = 'user123';
      const subscriptions: Subscription[] = [
        {
          id: 'sub1',
          userId: 'user123',
          name: 'Netflix',
          price: 15.99,
          currency: 'USD',
          billingCycle: 'monthly',
          nextPaymentDate: '2023-12-01',
          isActive: true,
        },
        {
          id: 'sub2',
          userId: 'user123',
          name: 'Spotify',
          price: 9.99,
          currency: 'USD',
          billingCycle: 'monthly',
          nextPaymentDate: '2023-12-15',
          isActive: true,
        },
      ];

      mockApiClient.get.mockResolvedValueOnce(subscriptions);

      const result = await subscriptionService.getAllSubscriptions(userId);

      expect(result).toEqual(subscriptions);
      expect(mockApiClient.get).toHaveBeenCalledWith(`/subscriptions?userId=${userId}`);
    });

    it('should throw error when fetching subscriptions fails', async () => {
      const userId = 'user123';
      const error = new Error('Failed to fetch subscriptions');
      mockApiClient.get.mockRejectedValueOnce(error);

      await expect(subscriptionService.getAllSubscriptions(userId)).rejects.toThrow('Failed to fetch subscriptions');
      expect(mockApiClient.get).toHaveBeenCalledWith(`/subscriptions?userId=${userId}`);
    });
  });

  describe('addSubscription', () => {
    it('should successfully add a new subscription', async () => {
      const createRequest: CreateSubscriptionRequest = {
        userId: 'user123',
        name: 'Netflix',
        price: 15.99,
        currency: 'USD',
        billingCycle: 'monthly',
        nextPaymentDate: '2023-12-01',
      };

      const newSubscription: Subscription = {
        id: 'sub1',
        ...createRequest,
        isActive: true,
      };

      mockApiClient.post.mockResolvedValueOnce(newSubscription);

      const result = await subscriptionService.addSubscription(createRequest);

      expect(result).toEqual(newSubscription);
      expect(mockApiClient.post).toHaveBeenCalledWith('/subscriptions', createRequest);
    });

    it('should throw error when adding subscription fails', async () => {
      const createRequest: CreateSubscriptionRequest = {
        userId: 'user123',
        name: 'Netflix',
        price: 15.99,
        currency: 'USD',
        billingCycle: 'monthly',
        nextPaymentDate: '2023-12-01',
      };

      const error = new Error('Failed to add subscription');
      mockApiClient.post.mockRejectedValueOnce(error);

      await expect(subscriptionService.addSubscription(createRequest)).rejects.toThrow('Failed to add subscription');
      expect(mockApiClient.post).toHaveBeenCalledWith('/subscriptions', createRequest);
    });
  });

  describe('deleteSubscription', () => {
    it('should successfully delete a subscription', async () => {
      const subscriptionId = 'sub123';
      mockApiClient.delete.mockResolvedValueOnce(undefined);

      await subscriptionService.deleteSubscription(subscriptionId);

      expect(mockApiClient.delete).toHaveBeenCalledWith(`/subscriptions/${subscriptionId}`);
    });

    it('should throw error when deleting subscription fails', async () => {
      const subscriptionId = 'sub123';
      const error = new Error('Failed to delete subscription');
      mockApiClient.delete.mockRejectedValueOnce(error);

      await expect(subscriptionService.deleteSubscription(subscriptionId)).rejects.toThrow('Failed to delete subscription');
      expect(mockApiClient.delete).toHaveBeenCalledWith(`/subscriptions/${subscriptionId}`);
    });
  });
});