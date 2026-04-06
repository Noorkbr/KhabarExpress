import React from 'react';
import { useLocation } from 'react-router-dom';
import { FiBell } from 'react-icons/fi';
import { useAuth } from '../../context/AuthContext';

const titles = {
  '/': 'Dashboard',
  '/orders': 'Orders',
  '/restaurants': 'Restaurants',
  '/users': 'Users',
  '/riders': 'Riders',
  '/payments': 'Payments',
  '/reports': 'Reports',
  '/promo-codes': 'Promo Codes',
  '/zones': 'Zones',
  '/settings': 'Settings',
};

export default function Header() {
  const { user } = useAuth();
  const { pathname } = useLocation();
  const title = titles[pathname] || 'Admin';

  return (
    <header className="h-16 bg-white border-b border-gray-100 flex items-center justify-between px-6 sticky top-0 z-10">
      <h1 className="text-xl font-semibold text-navy-900">{title}</h1>
      <div className="flex items-center gap-4">
        <button className="relative p-2 rounded-lg text-gray-500 hover:bg-gray-100">
          <FiBell />
        </button>
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-full bg-gold flex items-center justify-center text-navy-900 font-bold text-sm">
            {user?.name?.[0] || 'A'}
          </div>
          <span className="text-sm font-medium text-gray-700 hidden md:block">
            {user?.name || 'Admin'}
          </span>
        </div>
      </div>
    </header>
  );
}
