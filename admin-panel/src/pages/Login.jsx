import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';
import Spinner from '../components/shared/Spinner';

export default function LoginPage() {
  const { login, user } = useAuth();
  const navigate = useNavigate();
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  if (user) {
    navigate('/', { replace: true });
    return null;
  }

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!phone || !password) {
      toast.error('Phone and password are required');
      return;
    }
    setLoading(true);
    try {
      await login(phone, password);
      toast.success('Welcome back!');
      navigate('/');
    } catch (err) {
      toast.error(err.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-navy-900 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        {/* Logo */}
        <div className="text-center mb-8">
          <div className="w-16 h-16 bg-gold rounded-2xl flex items-center justify-center text-navy-900 font-black text-2xl mx-auto mb-4">
            KE
          </div>
          <h1 className="text-3xl font-bold text-white">KhabarExpress</h1>
          <p className="text-gold mt-1 text-sm">Admin Dashboard</p>
        </div>

        {/* Card */}
        <div className="bg-navy-800 rounded-2xl p-8 shadow-2xl border border-navy-700">
          <h2 className="text-white text-xl font-semibold mb-6">Sign in to Admin Panel</h2>

          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label className="block text-sm text-gray-400 mb-1.5">Phone Number</label>
              <input
                type="tel"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                placeholder="+8801883688374"
                className="w-full bg-navy-900 border border-navy-600 rounded-lg px-4 py-3 text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-gold focus:border-transparent transition-colors"
                autoComplete="username"
              />
            </div>

            <div>
              <label className="block text-sm text-gray-400 mb-1.5">Password</label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Enter your password"
                className="w-full bg-navy-900 border border-navy-600 rounded-lg px-4 py-3 text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-gold focus:border-transparent transition-colors"
                autoComplete="current-password"
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-gold text-navy-900 font-bold py-3 rounded-lg hover:bg-gold-600 transition-colors disabled:opacity-60 flex items-center justify-center gap-2"
            >
              {loading ? <Spinner size="sm" /> : null}
              {loading ? 'Signing in…' : 'Sign In'}
            </button>
          </form>

          <p className="text-center text-xs text-gray-500 mt-6">
            KhabarExpress © {new Date().getFullYear()} — Admin Access Only
          </p>
        </div>
      </div>
    </div>
  );
}