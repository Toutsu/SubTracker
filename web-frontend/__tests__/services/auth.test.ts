import { AuthService } from '../../services/auth';
import { apiClient } from '../../services/api';
import type { LoginRequest, RegisterRequest, AuthResponse } from '../../types/auth';

// Mock the apiClient
jest.mock('../../services/api', () => ({
  apiClient: {
    post: jest.fn(),
    setToken: jest.fn(),
  },
}));

describe('AuthService', () => {
  let authService: AuthService;
  const mockApiClient = apiClient as jest.Mocked<typeof apiClient>;

  beforeEach(() => {
    authService = new AuthService();
    jest.clearAllMocks();
  });

  describe('login', () => {
    it('should successfully login and set token', async () => {
      const loginRequest: LoginRequest = {
        username: 'testuser',
        password: 'testpass',
      };

      const authResponse: AuthResponse = {
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

      mockApiClient.post.mockResolvedValueOnce(authResponse);

      const result = await authService.login(loginRequest);

      expect(result).toEqual(authResponse);
      expect(mockApiClient.post).toHaveBeenCalledWith('/login', loginRequest);
      expect(mockApiClient.setToken).toHaveBeenCalledWith('test-token');
    });

    it('should handle login failure', async () => {
      const loginRequest: LoginRequest = {
        username: 'testuser',
        password: 'wrongpass',
      };

      const authResponse: AuthResponse = {
        success: false,
        message: 'Invalid credentials',
      };

      mockApiClient.post.mockResolvedValueOnce(authResponse);

      const result = await authService.login(loginRequest);

      expect(result).toEqual(authResponse);
      expect(mockApiClient.post).toHaveBeenCalledWith('/login', loginRequest);
      expect(mockApiClient.setToken).not.toHaveBeenCalled();
    });

    it('should throw error when api call fails', async () => {
      const loginRequest: LoginRequest = {
        username: 'testuser',
        password: 'testpass',
      };

      const error = new Error('Network error');
      mockApiClient.post.mockRejectedValueOnce(error);

      await expect(authService.login(loginRequest)).rejects.toThrow('Network error');
      expect(mockApiClient.post).toHaveBeenCalledWith('/login', loginRequest);
    });
  });

  describe('register', () => {
    it('should successfully register a new user', async () => {
      const registerRequest: RegisterRequest = {
        username: 'newuser',
        email: 'new@example.com',
        password: 'newpass',
      };

      const authResponse: AuthResponse = {
        success: true,
        message: 'Registration successful',
      };

      mockApiClient.post.mockResolvedValueOnce(authResponse);

      const result = await authService.register(registerRequest);

      expect(result).toEqual(authResponse);
      expect(mockApiClient.post).toHaveBeenCalledWith('/register', registerRequest);
    });

    it('should throw error when registration fails', async () => {
      const registerRequest: RegisterRequest = {
        username: 'newuser',
        email: 'new@example.com',
        password: 'newpass',
      };

      const error = new Error('Registration failed');
      mockApiClient.post.mockRejectedValueOnce(error);

      await expect(authService.register(registerRequest)).rejects.toThrow('Registration failed');
      expect(mockApiClient.post).toHaveBeenCalledWith('/register', registerRequest);
    });
  });

  describe('logout', () => {
    it('should clear the token', () => {
      authService.logout();
      expect(mockApiClient.setToken).toHaveBeenCalledWith(null);
    });
  });

  describe('isAuthenticated', () => {
    it('should return true when token exists in localStorage', () => {
      // Mock window and localStorage
      const localStorageMock = {
        getItem: jest.fn().mockReturnValue('test-token'),
      };
      Object.defineProperty(window, 'localStorage', { value: localStorageMock });

      const result = authService.isAuthenticated();
      expect(result).toBe(true);
    });

    it('should return false when no token in localStorage', () => {
      // Mock window and localStorage
      const localStorageMock = {
        getItem: jest.fn().mockReturnValue(null),
      };
      Object.defineProperty(window, 'localStorage', { value: localStorageMock });

      const result = authService.isAuthenticated();
      expect(result).toBe(false);
    });
  });
});