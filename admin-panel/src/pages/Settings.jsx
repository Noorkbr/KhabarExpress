import React, { useEffect, useState } from 'react';
import api from '../services/api';
import { PageSpinner } from '../components/shared/Spinner';
import toast from 'react-hot-toast';

export default function SettingsPage() {
  const [settings, setSettings] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    api.get('/admin/settings')
      .then(({ data }) => setSettings(data.data))
      .catch(() => toast.error('Failed to load settings'))
      .finally(() => setLoading(false));
  }, []);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setSettings((s) => ({ ...s, [name]: type === 'checkbox' ? checked : value }));
  };

  const handlePaymentToggle = (method) => {
    setSettings((s) => ({
      ...s,
      paymentMethods: { ...s.paymentMethods, [method]: !s.paymentMethods[method] },
    }));
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      await api.patch('/admin/settings', settings);
      toast.success('Settings saved');
    } catch {
      toast.error('Failed to save settings');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <PageSpinner />;
  if (!settings) return <div className="card text-gray-400 text-center py-12">Failed to load settings</div>;

  const paymentMethods = ['bkash', 'rocket', 'upay', 'sslcommerz', 'cod'];

  return (
    <div className="max-w-2xl space-y-6">
      <form onSubmit={handleSave} className="space-y-6">
        {/* Platform Settings */}
        <div className="card space-y-4">
          <h3 className="font-semibold text-navy-900">Platform Settings</h3>
          <div>
            <label className="block text-sm text-gray-600 mb-1">App Name</label>
            <input name="appName" value={settings.appName || ''} onChange={handleChange} className="input" />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm text-gray-600 mb-1">Base Delivery Fee (৳)</label>
              <input type="number" name="deliveryFee" value={settings.deliveryFee || ''} onChange={handleChange} className="input" min="0" />
            </div>
            <div>
              <label className="block text-sm text-gray-600 mb-1">Admin Profit Rate (%)</label>
              <input type="number" name="adminProfitRate" value={settings.adminProfitRate || ''} onChange={handleChange} className="input" min="0" max="100" step="0.1" />
            </div>
          </div>
          <div>
            <label className="block text-sm text-gray-600 mb-1">Minimum Order Amount (৳)</label>
            <input type="number" name="minOrderAmount" value={settings.minOrderAmount || ''} onChange={handleChange} className="input" min="0" />
          </div>
        </div>

        {/* Maintenance Mode */}
        <div className="card">
          <h3 className="font-semibold text-navy-900 mb-3">System Status</h3>
          <label className="flex items-center gap-3 cursor-pointer">
            <div className="relative">
              <input type="checkbox" className="sr-only" name="maintenanceMode" checked={!!settings.maintenanceMode} onChange={handleChange} />
              <div className={`w-11 h-6 rounded-full transition-colors ${settings.maintenanceMode ? 'bg-red-500' : 'bg-gray-300'}`} />
              <div className={`absolute top-0.5 left-0.5 w-5 h-5 rounded-full bg-white shadow transition-transform ${settings.maintenanceMode ? 'translate-x-5' : ''}`} />
            </div>
            <span className="text-sm font-medium">
              Maintenance Mode {settings.maintenanceMode ? <span className="text-red-500">(ON — App is unavailable to users)</span> : <span className="text-gray-500">(OFF)</span>}
            </span>
          </label>
        </div>

        {/* Payment Methods */}
        <div className="card">
          <h3 className="font-semibold text-navy-900 mb-3">Payment Gateways</h3>
          <div className="space-y-3">
            {paymentMethods.map((method) => (
              <label key={method} className="flex items-center gap-3 cursor-pointer">
                <div className="relative" onClick={() => handlePaymentToggle(method)}>
                  <input type="checkbox" className="sr-only" readOnly checked={!!settings.paymentMethods?.[method]} />
                  <div className={`w-11 h-6 rounded-full transition-colors ${settings.paymentMethods?.[method] ? 'bg-gold' : 'bg-gray-300'}`} />
                  <div className={`absolute top-0.5 left-0.5 w-5 h-5 rounded-full bg-white shadow transition-transform ${settings.paymentMethods?.[method] ? 'translate-x-5' : ''}`} />
                </div>
                <span className="text-sm font-medium capitalize">{method}</span>
              </label>
            ))}
          </div>
        </div>

        <button type="submit" disabled={saving} className="btn-primary w-full">
          {saving ? 'Saving…' : 'Save All Settings'}
        </button>
      </form>
    </div>
  );
}
