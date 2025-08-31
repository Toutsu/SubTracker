import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import LoginForm from '../../components/LoginForm';
import { authService } from '../../services/auth';

// Mock next/navigation
jest.mock('next/navigation', () => ({
  useRouter: () => ({
    push: jest.fn(),
  }),
}));

// Mock the authService
jest.mock('../../services/auth', () => ({
  authService: {
    login: jest.fn(),
  },
}));

describe('LoginForm', () => {
  const mockAuthService = authService as jest.Mocked<typeof authService>;
  const mockPush = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    (require('next/navigation').useRouter as jest.Mock).mockReturnValue({
      push: mockPush,
    });
  });

  it('should render the login form', () => {
    render(<LoginForm />);

    expect(screen.getByText('Вход')).toBeInTheDocument();
    expect(screen.getByText('Войдите в свой аккаунт SubTracker')).toBeInTheDocument();
    expect(screen.getByLabelText('Имя пользователя')).toBeInTheDocument();
    expect(screen.getByLabelText('Пароль')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Войти' })).toBeInTheDocument();
  });

  it('should display test credentials', () => {
    render(<LoginForm />);

    expect(screen.getByText('Тестовый аккаунт:')).toBeInTheDocument();
    expect(screen.getByText('Логин: user')).toBeInTheDocument();
    expect(screen.getByText('Пароль: user')).toBeInTheDocument();
  });

  it('should handle successful login', async () => {
    const loginResponse = {
      success: true,
      message: 'Login successful',
      token: 'test-token',
      user: {
        id: '1',
        username: 'testuser',
        email: 'test@example.com',
        passwordHash: 'hashed-password',
      },
    };

    mockAuthService.login.mockResolvedValueOnce(loginResponse);

    render(<LoginForm />);

    // Fill in the form
    fireEvent.change(screen.getByLabelText('Имя пользователя'), {
      target: { value: 'testuser' },
    });
    fireEvent.change(screen.getByLabelText('Пароль'), {
      target: { value: 'testpass' },
    });

    // Submit the form
    fireEvent.click(screen.getByRole('button', { name: 'Войти' }));

    // Check that authService.login was called
    await waitFor(() => {
      expect(mockAuthService.login).toHaveBeenCalledWith({
        username: 'testuser',
        password: 'testpass',
      });
    });

    // Check that router.push was called
    expect(mockPush).toHaveBeenCalledWith('/dashboard');
  });

  it('should display error message on login failure', async () => {
    const loginResponse = {
      success: false,
      message: 'Invalid credentials',
    };

    mockAuthService.login.mockResolvedValueOnce(loginResponse);

    render(<LoginForm />);

    // Fill in the form
    fireEvent.change(screen.getByLabelText('Имя пользователя'), {
      target: { value: 'testuser' },
    });
    fireEvent.change(screen.getByLabelText('Пароль'), {
      target: { value: 'wrongpass' },
    });

    // Submit the form
    fireEvent.click(screen.getByRole('button', { name: 'Войти' }));

    // Check that error message is displayed
    await waitFor(() => {
      expect(screen.getByText('Invalid credentials')).toBeInTheDocument();
    });
  });

  it('should display error message on network error', async () => {
    mockAuthService.login.mockRejectedValueOnce(new Error('Network error'));

    render(<LoginForm />);

    // Fill in the form
    fireEvent.change(screen.getByLabelText('Имя пользователя'), {
      target: { value: 'testuser' },
    });
    fireEvent.change(screen.getByLabelText('Пароль'), {
      target: { value: 'testpass' },
    });

    // Submit the form
    fireEvent.click(screen.getByRole('button', { name: 'Войти' }));

    // Check that error message is displayed
    await waitFor(() => {
      expect(screen.getByText('Ошибка соединения с сервером')).toBeInTheDocument();
    });
  });

  it('should disable submit button during login', async () => {
    mockAuthService.login.mockImplementationOnce(() => new Promise(resolve => setTimeout(resolve, 100)));

    render(<LoginForm />);

    // Fill in the form
    fireEvent.change(screen.getByLabelText('Имя пользователя'), {
      target: { value: 'testuser' },
    });
    fireEvent.change(screen.getByLabelText('Пароль'), {
      target: { value: 'testpass' },
    });

    // Submit the form
    const submitButton = screen.getByRole('button', { name: 'Войти' });
    fireEvent.click(submitButton);

    // Check that button is disabled during login
    expect(submitButton).toBeDisabled();
    expect(submitButton).toHaveTextContent('Вход...');

    // Wait for login to complete
    await waitFor(() => {
      expect(submitButton).not.toBeDisabled();
    });
  });
});