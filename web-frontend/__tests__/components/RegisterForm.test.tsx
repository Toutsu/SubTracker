import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import RegisterForm from '../../components/RegisterForm';
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
    register: jest.fn(),
  },
}));

describe('RegisterForm', () => {
  const mockAuthService = authService as jest.Mocked<typeof authService>;
  const mockPush = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    (require('next/navigation').useRouter as jest.Mock).mockReturnValue({
      push: mockPush,
    });
  });

  it('should render the registration form', () => {
    render(<RegisterForm />);

    expect(screen.getByText('Регистрация')).toBeInTheDocument();
    expect(screen.getByText('Создайте новый аккаунт в SubTracker')).toBeInTheDocument();
    expect(screen.getByLabelText('Имя пользователя')).toBeInTheDocument();
    expect(screen.getByLabelText('Email')).toBeInTheDocument();
    expect(screen.getByLabelText('Пароль')).toBeInTheDocument();
    expect(screen.getByLabelText('Подтвердите пароль')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Зарегистрироваться' })).toBeInTheDocument();
  });

  it('should display error when passwords do not match', async () => {
    render(<RegisterForm />);

    // Fill in the form with mismatched passwords
    fireEvent.change(screen.getByLabelText('Имя пользователя'), {
      target: { value: 'newuser' },
    });
    fireEvent.change(screen.getByLabelText('Email'), {
      target: { value: 'new@example.com' },
    });
    fireEvent.change(screen.getByLabelText('Пароль'), {
      target: { value: 'password123' },
    });
    fireEvent.change(screen.getByLabelText('Подтвердите пароль'), {
      target: { value: 'differentpassword' },
    });

    // Submit the form
    fireEvent.click(screen.getByRole('button', { name: 'Зарегистрироваться' }));

    // Check that error message is displayed
    expect(screen.getByText('Пароли не совпадают')).toBeInTheDocument();
  });

  it('should handle successful registration', async () => {
    const registerResponse = {
      success: true,
      message: 'Registration successful',
    };

    mockAuthService.register.mockResolvedValueOnce(registerResponse);

    render(<RegisterForm />);

    // Fill in the form
    fireEvent.change(screen.getByLabelText('Имя пользователя'), {
      target: { value: 'newuser' },
    });
    fireEvent.change(screen.getByLabelText('Email'), {
      target: { value: 'new@example.com' },
    });
    fireEvent.change(screen.getByLabelText('Пароль'), {
      target: { value: 'password123' },
    });
    fireEvent.change(screen.getByLabelText('Подтвердите пароль'), {
      target: { value: 'password123' },
    });

    // Submit the form
    fireEvent.click(screen.getByRole('button', { name: 'Зарегистрироваться' }));

    // Check that authService.register was called
    await waitFor(() => {
      expect(mockAuthService.register).toHaveBeenCalledWith({
        username: 'newuser',
        email: 'new@example.com',
        password: 'password123',
      });
    });

    // Check that success message is displayed
    expect(screen.getByText('Registration successful')).toBeInTheDocument();
  });

  it('should display error message on registration failure', async () => {
    const registerResponse = {
      success: false,
      message: 'Username already exists',
    };

    mockAuthService.register.mockResolvedValueOnce(registerResponse);

    render(<RegisterForm />);

    // Fill in the form
    fireEvent.change(screen.getByLabelText('Имя пользователя'), {
      target: { value: 'existinguser' },
    });
    fireEvent.change(screen.getByLabelText('Email'), {
      target: { value: 'existing@example.com' },
    });
    fireEvent.change(screen.getByLabelText('Пароль'), {
      target: { value: 'password123' },
    });
    fireEvent.change(screen.getByLabelText('Подтвердите пароль'), {
      target: { value: 'password123' },
    });

    // Submit the form
    fireEvent.click(screen.getByRole('button', { name: 'Зарегистрироваться' }));

    // Check that error message is displayed
    await waitFor(() => {
      expect(screen.getByText('Username already exists')).toBeInTheDocument();
    });
  });

  it('should display error message on network error', async () => {
    mockAuthService.register.mockRejectedValueOnce(new Error('Network error'));

    render(<RegisterForm />);

    // Fill in the form
    fireEvent.change(screen.getByLabelText('Имя пользователя'), {
      target: { value: 'newuser' },
    });
    fireEvent.change(screen.getByLabelText('Email'), {
      target: { value: 'new@example.com' },
    });
    fireEvent.change(screen.getByLabelText('Пароль'), {
      target: { value: 'password123' },
    });
    fireEvent.change(screen.getByLabelText('Подтвердите пароль'), {
      target: { value: 'password123' },
    });

    // Submit the form
    fireEvent.click(screen.getByRole('button', { name: 'Зарегистрироваться' }));

    // Check that error message is displayed
    await waitFor(() => {
      expect(screen.getByText('Ошибка соединения с сервером')).toBeInTheDocument();
    });
  });

  it('should disable submit button during registration', async () => {
    mockAuthService.register.mockImplementationOnce(() => new Promise(resolve => setTimeout(resolve, 100)));

    render(<RegisterForm />);

    // Fill in the form
    fireEvent.change(screen.getByLabelText('Имя пользователя'), {
      target: { value: 'newuser' },
    });
    fireEvent.change(screen.getByLabelText('Email'), {
      target: { value: 'new@example.com' },
    });
    fireEvent.change(screen.getByLabelText('Пароль'), {
      target: { value: 'password123' },
    });
    fireEvent.change(screen.getByLabelText('Подтвердите пароль'), {
      target: { value: 'password123' },
    });

    // Submit the form
    const submitButton = screen.getByRole('button', { name: 'Зарегистрироваться' });
    fireEvent.click(submitButton);

    // Check that button is disabled during registration
    expect(submitButton).toBeDisabled();
    expect(submitButton).toHaveTextContent('Регистрация...');

    // Wait for registration to complete
    await waitFor(() => {
      expect(submitButton).not.toBeDisabled();
    });
  });
});