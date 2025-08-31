import { renderHook, act } from '@testing-library/react';
import { useAuth } from '../../hooks/useAuth';
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
    register: jest.fn(),
    logout: jest.fn(),
    isAuthenticated: jest.fn(),
  },
}));

describe('useAuth', () => {
  const mockAuthService = authService as jest.Mocked<typeof authService>;
  const mockPush = jest.fn();
beforeEach(() => {
  jest.clearAllMocks();
  (require('next/navigation').useRouter as jest.Mock).mockImplementation(() => ({
    push: mockPush,
  }));
});


  describe('checkAuthStatus', () => {
    it('should set user when authenticated', async () => {
      const user = {
        id: '1',
        username: 'testuser',
        email: 'test@example.com',
        passwordHash: 'hashed-password',
      };
      
      mockAuthService.isAuthenticated.mockReturnValue(true);
      
      // Mock localStorage
      const localStorageMock = {
        getItem: jest.fn().mockReturnValue(JSON.stringify(user)),
        setItem: jest.fn(),
        removeItem: jest.fn(),
      };
      Object.defineProperty(window, 'localStorage', { value: localStorageMock });

      const { result } = renderHook(() => useAuth());

      // Wait for useEffect to complete
      await act(async () => {
        await new Promise(resolve => setTimeout(resolve, 0));
      });

      expect(result.current.user).toEqual(user);
      expect(result.current.isAuthenticated).toBe(true);
      expect(result.current.loading).toBe(false);
    });

    it('should not set user when not authenticated', async () => {
      mockAuthService.isAuthenticated.mockReturnValue(false);
      
      // Mock localStorage
      const localStorageMock = {
        getItem: jest.fn().mockReturnValue(null),
        setItem: jest.fn(),
        removeItem: jest.fn(),
      };
      Object.defineProperty(window, 'localStorage', { value: localStorageMock });

      const { result } = renderHook(() => useAuth());

      // Wait for useEffect to complete
      await act(async () => {
        await new Promise(resolve => setTimeout(resolve, 0));
      });

      expect(result.current.user).toBeNull();
      expect(result.current.isAuthenticated).toBe(false);
      expect(result.current.loading).toBe(false);
    });
  });

  describe('login', () => {
    it('should successfully login and set user', async () => {
      const user = {
        id: '1',
        username: 'testuser',
        email: 'test@example.com',
        passwordHash: 'hashed-password',
      };
      
      const loginResponse = {
        success: true,
        message: 'Login successful',
        token: 'test-token',
        user,
      };

      mockAuthService.login.mockResolvedValueOnce(loginResponse);
      
      // Mock localStorage
      const localStorageMock = {
        getItem: jest.fn(),
        setItem: jest.fn(),
        removeItem: jest.fn(),
      };
      Object.defineProperty(window, 'localStorage', { value: localStorageMock });

      const { result } = renderHook(() => useAuth());

      let loginResult;
      await act(async () => {
        loginResult = await result.current.login('testuser', 'testpass');
      });

      expect(loginResult).toEqual(loginResponse);
      expect(result.current.user).toEqual(user);
      expect(result.current.isAuthenticated).toBe(true);
      expect(localStorageMock.setItem).toHaveBeenCalledWith('user', JSON.stringify(user));
    });

    it('should throw error when login fails', async () => {
      const loginResponse = {
        success: false,
        message: 'Invalid credentials',
      };

      mockAuthService.login.mockResolvedValueOnce(loginResponse);

      const { result } = renderHook(() => useAuth());

      await expect(act(async () => {
        await result.current.login('testuser', 'wrongpass');
      })).rejects.toThrow('Invalid credentials');

      expect(result.current.user).toBeNull();
      expect(result.current.isAuthenticated).toBe(false);
    });
  });

  describe('register', () => {
    it('should successfully register', async () => {
      const registerResponse = {
        success: true,
        message: 'Registration successful',
      };

      mockAuthService.register.mockResolvedValueOnce(registerResponse);

      const { result } = renderHook(() => useAuth());

      let registerResult;
      await act(async () => {
        registerResult = await result.current.register('testuser', 'test@example.com', 'testpass');
      });

      expect(registerResult).toEqual(registerResponse);
    });

    it('should throw error when registration fails', async () => {
      const error = new Error('Registration failed');
      mockAuthService.register.mockRejectedValueOnce(error);

      const { result } = renderHook(() => useAuth());

      await expect(act(async () => {
        await result.current.register('testuser', 'test@example.com', 'testpass');
      })).rejects.toThrow('Registration failed');
    });
  });

  describe('logout', () => {
    it('should clear user and redirect to login', () => {
      // Mock localStorage
      const localStorageMock = {
        getItem: jest.fn(),
        setItem: jest.fn(),
        removeItem: jest.fn(),
      };
      Object.defineProperty(window, 'localStorage', { value: localStorageMock });

      const { result } = renderHook(() => useAuth());
      
      act(() => {
        result.current.logout();
      });

      expect(result.current.user).toBeNull();
      expect(result.current.isAuthenticated).toBe(false);
      expect(mockAuthService.logout).toHaveBeenCalled();
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('user');
      expect(mockPush).toHaveBeenCalledWith('/login');
    });
  });
});