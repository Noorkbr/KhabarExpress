import React, { useState } from 'react';
import {
  LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, Legend,
} from 'recharts';
import api from '../services/api';
import ExportButton from '../components/shared/ExportButton';
import { PageSpinner } from '../components/shared/Spinner';
import toast from 'react-hot-toast';
import { format, subDays } from 'date-fns';

export default function ReportsPage() {
  const [startDate, setStartDate] = useState(format(subDays(new Date(), 30), 'yyyy-MM-dd'));
  const [endDate, setEndDate] = useState(format(new Date(), 'yyyy-MM-dd'));
  const [type, setType] = useState('daily');
  const [analytics, setAnalytics] = useState(null);
  const [profit, setProfit] = useState(null);
  const [loading, setLoading] = useState(false);

  const fetchReports = async () => {
    setLoading(true);
    try {
      const [a, p] = await Promise.all([
        api.get(`/admin/analytics?startDate=${startDate}&endDate=${endDate}&type=${type}`),
        api.get('/admin/profit'),
      ]);
      setAnalytics(a.data.data);
      setProfit(p.data.data);
    } catch {
      toast.error('Failed to load reports');
    } finally {
      setLoading(false);
    }
  };

  const handleExportCSV = async () => {
    try {
      const resp = await api.get(
        `/admin/financial-report?startDate=${startDate}&endDate=${endDate}`,
        { responseType: 'blob' }
      );
      const url = URL.createObjectURL(new Blob([resp.data]));
      const a = document.createElement('a');
      a.href = url;
      a.download = `financial_report_${startDate}_${endDate}.csv`;
      a.click();
      URL.revokeObjectURL(url);
    } catch {
      toast.error('Export failed');
    }
  };

  const chartData = analytics?.ordersAnalytics?.map((d) => ({
    label: d._id.day ? `${d._id.month}/${d._id.day}` : `${d._id.month}/${d._id.year}`,
    orders: d.totalOrders,
    revenue: (d.totalRevenue || 0) / 100,
    cancelled: d.cancelledOrders,
    delivered: d.deliveredOrders,
  })) || [];

  return (
    <div className="space-y-6">
      {/* Controls */}
      <div className="card flex flex-wrap gap-3 items-end">
        <div>
          <label className="block text-xs text-gray-500 mb-1">Start Date</label>
          <input type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} className="input w-40" />
        </div>
        <div>
          <label className="block text-xs text-gray-500 mb-1">End Date</label>
          <input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} className="input w-40" />
        </div>
        <div>
          <label className="block text-xs text-gray-500 mb-1">Group By</label>
          <select value={type} onChange={(e) => setType(e.target.value)} className="input w-32">
            <option value="daily">Daily</option>
            <option value="monthly">Monthly</option>
          </select>
        </div>
        <button onClick={fetchReports} disabled={loading} className="btn-primary">
          {loading ? 'Loading…' : 'Generate Report'}
        </button>
        <ExportButton onClick={handleExportCSV} label="Export Financial CSV" />
      </div>

      {loading && <PageSpinner />}

      {analytics && (
        <>
          {/* Revenue Chart */}
          <div className="card">
            <h3 className="font-semibold text-navy-900 mb-4">Revenue & Orders</h3>
            <ResponsiveContainer width="100%" height={280}>
              <LineChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                <XAxis dataKey="label" tick={{ fontSize: 11 }} />
                <YAxis yAxisId="left" tick={{ fontSize: 11 }} tickFormatter={(v) => `৳${v.toFixed(0)}`} />
                <YAxis yAxisId="right" orientation="right" tick={{ fontSize: 11 }} />
                <Tooltip />
                <Legend />
                <Line yAxisId="left" type="monotone" dataKey="revenue" stroke="#D4A843" strokeWidth={2} name="Revenue (৳)" dot={false} />
                <Line yAxisId="right" type="monotone" dataKey="orders" stroke="#1B2838" strokeWidth={2} name="Orders" dot={false} />
              </LineChart>
            </ResponsiveContainer>
          </div>

          {/* Order Status Bar Chart */}
          <div className="card">
            <h3 className="font-semibold text-navy-900 mb-4">Delivered vs Cancelled</h3>
            <ResponsiveContainer width="100%" height={220}>
              <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                <XAxis dataKey="label" tick={{ fontSize: 11 }} />
                <YAxis tick={{ fontSize: 11 }} />
                <Tooltip />
                <Legend />
                <Bar dataKey="delivered" fill="#10b981" name="Delivered" radius={[2, 2, 0, 0]} />
                <Bar dataKey="cancelled" fill="#ef4444" name="Cancelled" radius={[2, 2, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>

          {/* Profit Summary */}
          {profit && (
            <div className="card">
              <h3 className="font-semibold text-navy-900 mb-4">Profit & Loss Summary</h3>
              <div className="grid sm:grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                <div className="p-4 bg-gray-50 rounded-lg">
                  <p className="text-gray-500 text-xs mb-1">Total Revenue</p>
                  <p className="text-xl font-bold">৳{((profit.totalPayments || 0) / 100).toFixed(2)}</p>
                </div>
                <div className="p-4 bg-gold/10 rounded-lg">
                  <p className="text-gray-500 text-xs mb-1">Admin Profit (5%)</p>
                  <p className="text-xl font-bold text-gold">৳{((profit.totalAdminProfit || 0) / 100).toFixed(2)}</p>
                </div>
                <div className="p-4 bg-blue-50 rounded-lg">
                  <p className="text-gray-500 text-xs mb-1">Restaurant Payouts</p>
                  <p className="text-xl font-bold text-blue-600">৳{((profit.totalRestaurantPayout || 0) / 100).toFixed(2)}</p>
                </div>
                <div className="p-4 bg-gray-50 rounded-lg">
                  <p className="text-gray-500 text-xs mb-1">Total Transactions</p>
                  <p className="text-xl font-bold">{profit.totalTransactions || 0}</p>
                </div>
              </div>
            </div>
          )}
        </>
      )}

      {!analytics && !loading && (
        <div className="card flex items-center justify-center h-40 text-gray-400 text-sm">
          Select a date range and click "Generate Report" to view analytics
        </div>
      )}
    </div>
  );
}
