import React, { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext(null);

// Hardcoded admin credentials
const ADMIN_PHONE = '+8801883688374';
const ADMIN_PASSWORD = 'admin123';
const MOCK_TOKEN = 'hardcoded-admin-token';
const MOCK_USER = { name: 'Admin', phone: ADMIN_PHONE, role: 'admin' };

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
    if (token === MOCK_TOKEN) {
      try {
        const storedUser = JSON.parse(localStorage.getItem('admin_user'));
        setUser(storedUser || MOCK_USER);
      } catch {
        setUser(MOCK_USER);
      }
    } else {
      localStorage.removeItem('admin_token');
      localStorage.removeItem('admin_user');
      setUser(null);
    }
    setLoading(false);
  }, []);

  const login = async (phone, password) => {
    if (phone === ADMIN_PHONE && password === ADMIN_PASSWORD) {
      localStorage.setItem('admin_token', MOCK_TOKEN);
      localStorage.setItem('admin_user', JSON.stringify(MOCK_USER));
      setUser(MOCK_USER);
      return MOCK_USER;
    }
    throw new Error('Invalid phone number or password');
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