import { render, screen, fireEvent } from '@testing-library/react';
import SubscriptionItem from '../../components/SubscriptionItem';

describe('SubscriptionItem', () => {
  const mockSubscription = {
    id: 'sub1',
    userId: 'user123',
    name: 'Netflix',
    price: 15.99,
    currency: 'USD',
    billingCycle: 'monthly' as const,
    nextPaymentDate: '2023-12-01',
    isActive: true,
  };

  const mockOnDelete = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render subscription details correctly', () => {
    render(<SubscriptionItem subscription={mockSubscription} onDelete={mockOnDelete} />);

    expect(screen.getByText('Netflix')).toBeInTheDocument();
    expect(screen.getByText('15.99 USD')).toBeInTheDocument();
    expect(screen.getByText('Ежемесячно')).toBeInTheDocument();
    expect(screen.getByText('2023-12-01')).toBeInTheDocument();
    expect(screen.getByText('Активна')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Удалить' })).toBeInTheDocument();
  });

  it('should render inactive subscription status correctly', () => {
    const inactiveSubscription = {
      ...mockSubscription,
      isActive: false,
    };

    render(<SubscriptionItem subscription={inactiveSubscription} onDelete={mockOnDelete} />);

    expect(screen.getByText('Неактивна')).toBeInTheDocument();
  });

  it('should translate billing cycle correctly', () => {
    const yearlySubscription = {
      ...mockSubscription,
      billingCycle: 'yearly' as const,
    };

    const weeklySubscription = {
      ...mockSubscription,
      billingCycle: 'weekly' as const,
    };

    const { rerender } = render(<SubscriptionItem subscription={mockSubscription} onDelete={mockOnDelete} />);
    expect(screen.getByText('Ежемесячно')).toBeInTheDocument();

    rerender(<SubscriptionItem subscription={yearlySubscription} onDelete={mockOnDelete} />);
    expect(screen.getByText('Ежегодно')).toBeInTheDocument();

    rerender(<SubscriptionItem subscription={weeklySubscription} onDelete={mockOnDelete} />);
    expect(screen.getByText('Еженедельно')).toBeInTheDocument();
  });

  it('should call onDelete when delete button is clicked', () => {
    render(<SubscriptionItem subscription={mockSubscription} onDelete={mockOnDelete} />);

    fireEvent.click(screen.getByRole('button', { name: 'Удалить' }));

    expect(mockOnDelete).toHaveBeenCalledWith('sub1');
  });
});