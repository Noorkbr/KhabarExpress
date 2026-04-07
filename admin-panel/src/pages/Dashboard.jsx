import React, { useEffect, useState } from 'react';
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend,
} from 'recharts';
import api from '../services/api';
import { PageSpinner } from '../components/shared/Spinner';
import StatusBadge from '../components/shared/StatusBadge';
import toast from 'react-hot-toast';

const KPI_COLORS = ['#D4A843', '#1B2838', '#10b981', '#3b82f6', '#f59e0b', '#8b5cf6'];

function KPICard({ label, value, sub, color }) {
  return (
    <div className="card flex flex-col gap-1" style={{ borderTop: `3px solid ${color}` }}>
      <p className="text-sm text-gray-500">{label}</p>
      <p className="text-2xl font-bold text-navy-900">{value}</p>
      {sub && <p className="text-xs text-gray-400">{sub}</p>}
    </div>
  );
}

export default function DashboardPage() {
  const [stats, setStats] = useState(null);
  const [revenue, setRevenue] = useState([]);
  const [activities, setActivities] = useState([]);
  const [profit, setProfit] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      api.get('/admin/dashboard'),
      api.get('/admin/revenue'),
      api.get('/admin/activities?limit=10'),
      api.get('/admin/profit'),
    ])
      .then(([s, r, a, p]) => {
        setStats(s.data.data);
        setRevenue(r.data.data || []);
        setActivities(a.data.data?.orders || []);
        setProfit(p.data.data);
      })
      .catch((err) => {
        console.error('Dashboard load error:', err);
        toast.error('Failed to load dashboard data');
      })
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <PageSpinner />;

  const orderStatusData = stats
    ? [
        { name: 'Active', value: stats.active?.orders || 0, color: '#3b82f6' },
        { name: 'Delivered', value: Math.max(0, (stats.totals?.orders || 0) - (stats.active?.orders || 0)), color: '#10b981' },
        { name: 'Pending', value: stats.pending?.restaurants || 0, color: '#f59e0b' },
      ]
    : [];

  return (
    <div className="space-y-6">
      {/* KPI Cards */}
      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
        <KPICard label="Today's Orders" value={stats?.today?.orders ?? '—'} color={KPI_COLORS[0]} />
        <KPICard
          label="Today's Revenue"
          value={`৳${((stats?.today?.revenue || 0) / 100).toFixed(0)}`}
          color={KPI_COLORS[1]}
        />
        <KPICard label="Total Users" value={stats?.totals?.users ?? '—'} color={KPI_COLORS[2]} />
        <KPICard label="Restaurants" value={stats?.totals?.restaurants ?? '—'} color={KPI_COLORS[3]} />
        <KPICard label="Riders" value={stats?.totals?.riders ?? '—'} color={KPI_COLORS[4]} />
        <KPICard label="Active Orders" value={stats?.active?.orders ?? '—'} color={KPI_COLORS[5]} />
      </div>

      {/* Charts Row */}
      <div className="grid lg:grid-cols-3 gap-6">
        {/* Revenue Line Chart */}
        <div className="card lg:col-span-2">
          <h3 className="font-semibold text-navy-900 mb-4">Revenue (Last 30 Days)</h3>
          {revenue.length > 0 ? (
            <ResponsiveContainer width="100%" height={220}>
              <LineChart data={revenue}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                <XAxis
                  dataKey="_id"
                  tick={{ fontSize: 11 }}
                  tickFormatter={(d) => `${d?.month}/${d?.day}`}
                />
                <YAxis tick={{ fontSize: 11 }} tickFormatter={(v) => `৳${(v / 100).toFixed(0)}`} />
                <Tooltip formatter={(v) => [`৳${(v / 100).toFixed(2)}`, 'Revenue']} />
                <Line type="monotone" dataKey="totalRevenue" stroke="#D4A843" strokeWidth={2} dot={false} />
              </LineChart>
            </ResponsiveContainer>
          ) : (
            <div className="flex items-center justify-center h-48 text-gray-400 text-sm">
              No revenue data available
            </div>
          )}
        </div>

        {/* Pie Chart */}
        <div className="card">
          <h3 className="font-semibold text-navy-900 mb-4">Order Status</h3>
          <ResponsiveContainer width="100%" height={220}>
            <PieChart>
              <Pie data={orderStatusData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={75}>
                {orderStatusData.map((entry, i) => (
                  <Cell key={i} fill={entry.color} />
                ))}
              </Pie>
              <Legend />
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Profit Summary & Recent Orders */}
      <div className="grid lg:grid-cols-3 gap-6">
        {/* Profit Summary */}
        {profit && (
          <div className="card">
            <h3 className="font-semibold text-navy-900 mb-4">Profit Summary</h3>
            <div className="space-y-3 text-sm">
              <div className="flex justify-between">
                <span className="text-gray-500">Total Payments</span>
                <span className="font-semibold">৳{((profit.totalPayments || 0) / 100).toFixed(2)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Admin Profit (5%)</span>
                <span className="font-semibold text-gold">৳{((profit.totalAdminProfit || 0) / 100).toFixed(2)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-500">Restaurant Payouts</span>
                <span className="font-semibold">৳{((profit.totalRestaurantPayout || 0) / 100).toFixed(2)}</span>
              </div>
              <div className="pt-2 border-t border-gray-100 flex justify-between">
                <span className="text-gray-500">Total Transactions</span>
                <span className="font-semibold">{profit.totalTransactions || 0}</span>
              </div>
            </div>
          </div>
        )}

        {/* Recent Orders */}
        <div className={`card ${profit ? 'lg:col-span-2' : 'lg:col-span-3'}`}>
          <h3 className="font-semibold text-navy-900 mb-4">Recent Orders</h3>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-gray-500 border-b border-gray-100">
                  <th className="pb-2 font-medium">Order #</th>
                  <th className="pb-2 font-medium">Customer</th>
                  <th className="pb-2 font-medium">Restaurant</th>
                  <th className="pb-2 font-medium">Amount</th>
                  <th className="pb-2 font-medium">Status</th>
                </tr>
              </thead>
              <tbody>
                {activities.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="text-center text-gray-400 py-8">No recent orders</td>
                  </tr>
                ) : (
                  activities.map((o) => (
                    <tr key={o._id} className="border-b border-gray-50 last:border-0">
                      <td className="py-2 font-mono text-xs">{o.orderNumber}</td>
                      <td className="py-2">{o.userId?.name || o.user?.name || '—'}</td>
                      <td className="py-2">{o.restaurantId?.name || o.restaurant?.name || '—'}</td>
                      <td className="py-2">৳{((o.total || o.totalAmount || 0) / 100).toFixed(2)}</td>
                      <td className="py-2"><StatusBadge status={o.status} /></td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}
