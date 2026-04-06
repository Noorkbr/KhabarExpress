import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AuthProvider } from './context/AuthContext';
import AdminLayout from './components/layout/AdminLayout';

// Pages
import LoginPage from './pages/Login';
import DashboardPage from './pages/Dashboard';
import OrdersPage from './pages/Orders';
import RestaurantsPage from './pages/Restaurants';
import UsersPage from './pages/Users';
import RidersPage from './pages/Riders';
import PaymentsPage from './pages/Payments';
import ReportsPage from './pages/Reports';
import PromoCodesPage from './pages/PromoCodes';
import ZonesPage from './pages/Zones';
import SettingsPage from './pages/Settings';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Toaster
          position="top-right"
          toastOptions={{
            style: { background: '#1B2838', color: '#fff', fontSize: '14px' },
            success: { iconTheme: { primary: '#D4A843', secondary: '#1B2838' } },
          }}
        />
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route element={<AdminLayout />}>
            <Route path="/" element={<DashboardPage />} />
            <Route path="/orders" element={<OrdersPage />} />
            <Route path="/restaurants" element={<RestaurantsPage />} />
            <Route path="/users" element={<UsersPage />} />
            <Route path="/riders" element={<RidersPage />} />
            <Route path="/payments" element={<PaymentsPage />} />
            <Route path="/reports" element={<ReportsPage />} />
            <Route path="/promo-codes" element={<PromoCodesPage />} />
            <Route path="/zones" element={<ZonesPage />} />
            <Route path="/settings" element={<SettingsPage />} />
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
