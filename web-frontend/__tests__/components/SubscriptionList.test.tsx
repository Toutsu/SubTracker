import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import SubscriptionList from '../../components/SubscriptionList';
import { subscriptionService } from '../../services/subscriptions';

// Mock the subscriptionService
jest.mock('../../services/subscriptions', () => ({
  subscriptionService: {
    getAllSubscriptions: jest.fn(),
    deleteSubscription: jest.fn(),
  },
}));

describe('SubscriptionList', () => {
  const mockSubscriptionService = subscriptionService as jest.Mocked<typeof subscriptionService>;
  const userId = 'user123';

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render loading state initially', () => {
    // Mock the API call to not resolve immediately
    mockSubscriptionService.getAllSubscriptions.mockImplementationOnce(() => new Promise(() => {}));

    render(<SubscriptionList userId={userId} />);

    expect(screen.getByText('Загрузка подписок...')).toBeInTheDocument();
  });

  it('should render subscriptions when loaded', async () => {
    const subscriptions = [
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

    mockSubscriptionService.getAllSubscriptions.mockResolvedValueOnce(subscriptions);

    render(<SubscriptionList userId={userId} />);

    // Wait for subscriptions to load
    await waitFor(() => {
      expect(screen.getByText('Netflix')).toBeInTheDocument();
      expect(screen.getByText('Spotify')).toBeInTheDocument();
    });

    // Check that subscription details are displayed
    expect(screen.getByText('15.99 USD')).toBeInTheDocument();
    expect(screen.getByText('9.99 USD')).toBeInTheDocument();
  });

  it('should render empty state when no subscriptions', async () => {
    mockSubscriptionService.getAllSubscriptions.mockResolvedValueOnce([]);

    render(<SubscriptionList userId={userId} />);

    // Wait for subscriptions to load
    await waitFor(() => {
      expect(screen.getByText('У вас пока нет подписок')).toBeInTheDocument();
    });

    expect(screen.getByRole('button', { name: 'Добавить первую подписку' })).toBeInTheDocument();
  });

  it('should display error message when loading fails', async () => {
    mockSubscriptionService.getAllSubscriptions.mockRejectedValueOnce(new Error('Failed to load'));

    render(<SubscriptionList userId={userId} />);

    // Wait for error to be displayed
    await waitFor(() => {
      expect(screen.getByText('Не удалось загрузить подписки')).toBeInTheDocument();
    });
  });

  it('should handle subscription deletion', async () => {
    const subscriptions = [
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

    // First call returns subscriptions, second call returns empty array after deletion
    mockSubscriptionService.getAllSubscriptions
      .mockResolvedValueOnce(subscriptions)
      .mockResolvedValueOnce([]);

    mockSubscriptionService.deleteSubscription.mockResolvedValueOnce(undefined);

    render(<SubscriptionList userId={userId} />);

    // Wait for subscriptions to load
    await waitFor(() => {
      expect(screen.getByText('Netflix')).toBeInTheDocument();
    });

    // Click delete button
    fireEvent.click(screen.getByRole('button', { name: 'Удалить' }));

    // Check that deleteSubscription was called
    await waitFor(() => {
      expect(mockSubscriptionService.deleteSubscription).toHaveBeenCalledWith('sub1');
    });

    // Check that subscriptions are reloaded (resulting in empty state)
    await waitFor(() => {
      expect(screen.getByText('У вас пока нет подписок')).toBeInTheDocument();
    });
  });

  it('should display error message when deletion fails', async () => {
    const subscriptions = [
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
    mockSubscriptionService.deleteSubscription.mockRejectedValueOnce(new Error('Failed to delete'));

    render(<SubscriptionList userId={userId} />);

    // Wait for subscriptions to load
    await waitFor(() => {
      expect(screen.getByText('Netflix')).toBeInTheDocument();
    });

    // Click delete button
    fireEvent.click(screen.getByRole('button', { name: 'Удалить' }));

    // Check that error message is displayed
    await waitFor(() => {
      expect(screen.getByText('Не удалось удалить подписку')).toBeInTheDocument();
    });
  });
});