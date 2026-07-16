const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

async function apiClient(endpoint, options = {}) {
  const token = localStorage.getItem('jwt');

  const config = {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token && { Authorization: `Bearer ${token}` }),
      ...options.headers,
    },
  };

  const response = await fetch(`${BASE_URL}${endpoint}`, config);

  if (response.status === 401) {
    localStorage.removeItem('jwt');
    localStorage.removeItem('user');
    if (window.location.pathname !== '/login') {
      window.location.href = '/login';
    }
    throw new Error('Session expired. Please log in again.');
  }

  if (!response.ok) {
    const errorBody = await response.json().catch(() => ({}));
    throw new Error(errorBody.message || `Request failed (${response.status})`);
  }

  const text = await response.text();
  return text ? JSON.parse(text) : null;
}

export default apiClient;
