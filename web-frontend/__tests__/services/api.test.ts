import { ApiClient } from '../../services/api';

// Мock fetch globally
global.fetch = jest.fn();

describe('ApiClient', () => {
  let apiClient: ApiClient;
  const baseUrl = 'http://localhost:8080';

  beforeEach(() => {
    apiClient = new ApiClient(baseUrl);
    (fetch as jest.Mock).mockClear();
  });

  describe('getHeaders', () => {
    it('should return default headers without token', () => {
      const headers = (apiClient as any).getHeaders();
      expect(headers).toEqual({
        'Content-Type': 'application/json',
      });
    });

    it('should include Authorization header when token is set', () => {
      const token = 'test-token';
      apiClient.setToken(token);
      const headers = (apiClient as any).getHeaders();
      expect(headers).toEqual({
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
      });
    });
  });

  describe('request', () => {
    it('should make a successful request', async () => {
      const mockResponse = { data: 'test' };
      (fetch as jest.Mock).mockResolvedValueOnce({
        ok: true,
        json: jest.fn().mockResolvedValueOnce(mockResponse),
      });

      const result = await (apiClient as any).request('/test');
      expect(result).toEqual(mockResponse);
      expect(fetch).toHaveBeenCalledWith(
        `${baseUrl}/test`,
        expect.objectContaining({
          headers: {
            'Content-Type': 'application/json',
          },
        })
      );
    });

    it('should throw an error when response is not ok', async () => {
      (fetch as jest.Mock).mockResolvedValueOnce({
        ok: false,
        status: 500,
      });

      await expect((apiClient as any).request('/test')).rejects.toThrow('HTTP error! status: 500');
    });

    it('should clear token and throw error on 401 response', async () => {
      (fetch as jest.Mock).mockResolvedValueOnce({
        ok: false,
        status: 401,
      });

      // Mock localStorage
      const localStorageMock = {
        getItem: jest.fn(),
        setItem: jest.fn(),
        removeItem: jest.fn(),
      };
      Object.defineProperty(window, 'localStorage', { value: localStorageMock });

      await expect((apiClient as any).request('/test')).rejects.toThrow('Требуется аутентификация');
      expect(apiClient['token']).toBeNull();
    });
  });

  describe('HTTP methods', () => {
    beforeEach(() => {
      (fetch as jest.Mock).mockResolvedValue({
        ok: true,
        json: jest.fn().mockResolvedValueOnce({}),
      });
    });

    it('should make GET request', async () => {
      await apiClient.get('/test');
      expect(fetch).toHaveBeenCalledWith(
        `${baseUrl}/test`,
        expect.objectContaining({ method: 'GET' })
      );
    });

    it('should make POST request', async () => {
      const data = { test: 'data' };
      await apiClient.post('/test', data);
      expect(fetch).toHaveBeenCalledWith(
        `${baseUrl}/test`,
        expect.objectContaining({
          method: 'POST',
          body: JSON.stringify(data),
        })
      );
    });

    it('should make PUT request', async () => {
      const data = { test: 'data' };
      await apiClient.put('/test', data);
      expect(fetch).toHaveBeenCalledWith(
        `${baseUrl}/test`,
        expect.objectContaining({
          method: 'PUT',
          body: JSON.stringify(data),
        })
      );
    });

    it('should make DELETE request', async () => {
      await apiClient.delete('/test');
      expect(fetch).toHaveBeenCalledWith(
        `${baseUrl}/test`,
        expect.objectContaining({ method: 'DELETE' })
      );
    });
  });
});