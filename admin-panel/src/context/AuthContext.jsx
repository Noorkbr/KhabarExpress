import React, { createContext, useContext, useState, useEffect } from 'react';

const ADMIN_PHONE = '+8801883688374';
const ADMIN_PASSWORD = 'admin123';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem('admin_user'));
    } catch {
      return null;
    }
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('admin_token');
    if (token) {
      try {
        const storedUser = JSON.parse(localStorage.getItem('admin_user'));
        setUser(storedUser);
      } catch {
        localStorage.removeItem('admin_token');
        localStorage.removeItem('admin_user');
        setUser(null);
      }
    }
    setLoading(false);
  }, []);

  const login = async (phone, password) => {
    if (phone !== ADMIN_PHONE || password !== ADMIN_PASSWORD) {
      throw new Error('Invalid credentials');
    }
    const mockUser = { phone: ADMIN_PHONE, role: 'admin', name: 'Admin' };
    localStorage.setItem('admin_token', 'admin-token');
    localStorage.setItem('admin_user', JSON.stringify(mockUser));
    setUser(mockUser);
    return mockUser;
  };

  const logout = () => {
    localStorage.removeItem('admin_token');
    localStorage.removeItem('admin_user');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
