import React, { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import {
  FiHome, FiShoppingBag, FiUsers, FiMapPin, FiDollarSign,
  FiBarChart2, FiTag, FiSettings, FiLogOut, FiTruck, FiMenu, FiX,
} from 'react-icons/fi';
import { MdRestaurantMenu } from 'react-icons/md';
import { useAuth } from '../../context/AuthContext';
import toast from 'react-hot-toast';

const nav = [
  { to: '/', icon: FiHome, label: 'Dashboard', exact: true },
  { to: '/orders', icon: FiShoppingBag, label: 'Orders' },
  { to: '/restaurants', icon: MdRestaurantMenu, label: 'Restaurants' },
  { to: '/users', icon: FiUsers, label: 'Users' },
  { to: '/riders', icon: FiTruck, label: 'Riders' },
  { to: '/payments', icon: FiDollarSign, label: 'Payments' },
  { to: '/reports', icon: FiBarChart2, label: 'Reports' },
  { to: '/promo-codes', icon: FiTag, label: 'Promo Codes' },
  { to: '/zones', icon: FiMapPin, label: 'Zones' },
  { to: '/settings', icon: FiSettings, label: 'Settings' },
];

export default function Sidebar() {
  const { logout, user } = useAuth();
  const navigate = useNavigate();
  const [collapsed, setCollapsed] = useState(false);

  const handleLogout = () => {
    logout();
    toast.success('Logged out');
    navigate('/login');
  };

  return (
    <aside
      className={`flex flex-col bg-navy-900 text-white transition-all duration-300 ${
        collapsed ? 'w-16' : 'w-60'
      } min-h-screen sticky top-0`}
    >
      {/* Logo */}
      <div className="flex items-center gap-3 px-4 py-5 border-b border-navy-700">
        <div className="w-8 h-8 bg-gold rounded-lg flex-shrink-0 flex items-center justify-center font-bold text-navy-900 text-sm">
          KE
        </div>
        {!collapsed && (
          <div>
            <p className="font-bold text-sm leading-tight">KhabarExpress</p>
            <p className="text-gold text-xs">Admin Panel</p>
          </div>
        )}
        <button
          onClick={() => setCollapsed(!collapsed)}
          className="ml-auto text-gray-400 hover:text-white"
        >
          {collapsed ? <FiMenu /> : <FiX />}
        </button>
      </div>

      {/* Nav */}
      <nav className="flex-1 py-4 space-y-0.5 overflow-y-auto">
        {nav.map(({ to, icon: Icon, label, exact }) => (
          <NavLink
            key={to}
            to={to}
            end={exact}
            className={({ isActive }) =>
              `flex items-center gap-3 px-4 py-2.5 text-sm transition-colors ${
                isActive
                  ? 'bg-gold/20 text-gold font-medium border-r-2 border-gold'
                  : 'text-gray-400 hover:bg-navy-700 hover:text-white'
              }`
            }
          >
            <Icon className="flex-shrink-0 text-lg" />
            {!collapsed && label}
          </NavLink>
        ))}
      </nav>

      {/* User & Logout */}
      <div className="border-t border-navy-700 p-4">
        {!collapsed && user && (
          <div className="mb-3">
            <p className="text-sm font-medium truncate">{user.name}</p>
            <p className="text-xs text-gray-400 truncate">{user.phone}</p>
          </div>
        )}
        <button
          onClick={handleLogout}
          className="flex items-center gap-2 text-sm text-gray-400 hover:text-red-400 transition-colors w-full"
        >
          <FiLogOut className="flex-shrink-0" />
          {!collapsed && 'Logout'}
        </button>
      </div>
    </aside>
  );
}
