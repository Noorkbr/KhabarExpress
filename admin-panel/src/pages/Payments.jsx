import React, { useEffect, useState, useCallback } from 'react';
import {
  PieChart, Pie, Cell, Legend, Tooltip, ResponsiveContainer,
  LineChart, Line, XAxis, YAxis, CartesianGrid,
} from 'recharts';
import api from '../services/api';
import { PageSpinner } from '../components/shared/Spinner';
import StatusBadge from '../components/shared/StatusBadge';
import Pagination from '../components/shared/Pagination';
import ExportButton, { exportToCSV } from '../components/shared/ExportButton';
import toast from 'react-hot-toast';
import { format } from 'date-fns';

const METHODS = ['bkash', 'nagad', 'rocket', 'upay', 'card', 'cod'];
const STATUSES = ['pending', 'paid', 'failed', 'refunded'];
const PIE_COLORS = ['#D4A843', '#1B2838', '#10b981', '#3b82f6', '#f59e0b', '#8b5cf6'];

function SummaryCard({ label, value, color }) {
  return (
    <div className="card" style={{ borderTop: `3px solid ${color}` }}>
      <p className="text-sm text-gray-500">{label}</p>
      <p className="text-2xl font-bold text-navy-900 mt-1">{value}</p>
    </div>
  );
}

export default function PaymentsPage() {
  const [payments, setPayments] = useState([]);
  const [pagination, setPagination] = useState({ page: 1, pages: 1, total: 0 });
  const [loading, setLoading] = useState(true);
  const [profit, setProfit] = useState(null);
  const [revenue, setRevenue] = useState([]);
  const [filters, setFilters] = useState({ method: '', status: '', startDate: '', endDate: '' });
  const [page, setPage] = useState(1);

  const fetchPayments = useCallback(async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams({ page, limit: 20 });
      Object.entries(filters).forEach(([k, v]) => v && params.set(k, v));
      const { data } = await api.get(`/admin/payments?${params}`);
      setPayments(data.data || []);
      setPagination(data.pagination || {});
    } catch {
      toast.error('Failed to load payments');
    } finally {
      setLoading(false);
    }
  }, [page, filters]);

  useEffect(() => {
    fetchPayments();
    api.get('/admin/profit').then(({ data }) => setProfit(data.data)).catch(() => {});
    api.get('/admin/revenue').then(({ data }) => setRevenue(data.data || [])).catch(() => {});
  }, [fetchPayments]);

  const handleFilterChange = (e) => {
    setFilters((f) => ({ ...f, [e.target.name]: e.target.value }));
    setPage(1);
  };

  const handleExport = () => {
    exportToCSV('payments', payments.map((p) => ({
      'Transaction ID': p.gateway?.transactionId || p._id,
      'Order #': p.order?.orderNumber || '',
      Customer: p.user?.name || '',
      Phone: p.user?.phone || '',
      Amount: `৳${((p.amount || 0) / 100).toFixed(2)}`,
      Method: p.method,
      Status: p.status,
      'Admin Profit': `৳${((p.adminProfit || 0) / 100).toFixed(2)}`,
      Date: p.createdAt ? format(new Date(p.createdAt), 'yyyy-MM-dd HH:mm') : '',
    })));
  };

  // Build method breakdown data from profit analytics
  const methodData = profit?.byMethod?.map((m, i) => ({
    name: m._id || 'unknown',
    value: m.count || 0,
    color: PIE_COLORS[i % PIE_COLORS.length],
  })) || [];

  return (
    <div className="space-y-6">
      {/* Summary Cards */}
      {profit && (
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <SummaryCard label="Total Revenue" value={`৳${((profit.totalPayments || 0) / 100).toFixed(0)}`} color="#D4A843" />
          <SummaryCard label="Admin Profit (5%)" value={`৳${((profit.totalAdminProfit || 0) / 100).toFixed(0)}`} color="#10b981" />
          <SummaryCard label="Restaurant Payouts" value={`৳${((profit.totalRestaurantPayout || 0) / 100).toFixed(0)}`} color="#3b82f6" />
          <SummaryCard label="Transactions" value={profit.totalTransactions || 0} color="#8b5cf6" />
        </div>
      )}

      {/* Charts Row */}
      <div className="grid lg:grid-cols-3 gap-6">
        <div className="card lg:col-span-2">
          <h3 className="font-semibold text-navy-900 mb-4">Revenue Trend</h3>
          {revenue.length > 0 ? (
            <ResponsiveContainer width="100%" height={200}>
              <LineChart data={revenue}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                <XAxis dataKey="_id" tick={{ fontSize: 11 }} tickFormatter={(d) => `${d?.month}/${d?.day}`} />
                <YAxis tick={{ fontSize: 11 }} tickFormatter={(v) => `৳${(v / 100).toFixed(0)}`} />
                <Tooltip formatter={(v) => [`৳${(v / 100).toFixed(2)}`, 'Revenue']} />
                <Line type="monotone" dataKey="totalRevenue" stroke="#D4A843" strokeWidth={2} dot={false} />
              </LineChart>
            </ResponsiveContainer>
          ) : (
            <div className="flex items-center justify-center h-48 text-gray-400 text-sm">No data</div>
          )}
        </div>
        <div className="card">
          <h3 className="font-semibold text-navy-900 mb-4">Payment Methods</h3>
          {methodData.length > 0 ? (
            <ResponsiveContainer width="100%" height={200}>
              <PieChart>
                <Pie data={methodData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={70}>
                  {methodData.map((entry, i) => <Cell key={i} fill={entry.color} />)}
                </Pie>
                <Legend />
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          ) : (
            <div className="flex items-center justify-center h-48 text-gray-400 text-sm">No data</div>
          )}
        </div>
      </div>

      {/* Filters */}
      <div className="card flex flex-wrap gap-3">
        <select name="method" value={filters.method} onChange={handleFilterChange} className="input w-36">
          <option value="">All Methods</option>
          {METHODS.map((m) => <option key={m} value={m}>{m}</option>)}
        </select>
        <select name="status" value={filters.status} onChange={handleFilterChange} className="input w-36">
          <option value="">All Statuses</option>
          {STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
        </select>
        <input type="date" name="startDate" value={filters.startDate} onChange={handleFilterChange} className="input w-40" />
        <input type="date" name="endDate" value={filters.endDate} onChange={handleFilterChange} className="input w-40" />
        <ExportButton onClick={handleExport} />
      </div>

      {/* Table */}
      <div className="card overflow-x-auto p-0">
        <table className="w-full text-sm">
          <thead className="bg-gray-50">
            <tr className="text-left text-gray-500 border-b border-gray-100">
              {['Transaction ID', 'Order #', 'Customer', 'Amount', 'Method', 'Status', 'Admin Profit', 'Date'].map((h) => (
                <th key={h} className="px-4 py-3 font-medium">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan={8} className="text-center py-12"><PageSpinner /></td></tr>
            ) : payments.length === 0 ? (
              <tr><td colSpan={8} className="text-center py-12 text-gray-400">No payments found</td></tr>
            ) : (
              payments.map((p) => (
                <tr key={p._id} className="border-b border-gray-50 hover:bg-gray-50">
                  <td className="px-4 py-3 font-mono text-xs">{p.gateway?.transactionId || p._id?.slice(-8)}</td>
                  <td className="px-4 py-3 font-mono text-xs">{p.order?.orderNumber || '—'}</td>
                  <td className="px-4 py-3">
                    <div>{p.user?.name || '—'}</div>
                    <div className="text-xs text-gray-400">{p.user?.phone}</div>
                  </td>
                  <td className="px-4 py-3 font-medium">৳{((p.amount || 0) / 100).toFixed(2)}</td>
                  <td className="px-4 py-3 capitalize">{p.method}</td>
                  <td className="px-4 py-3"><StatusBadge status={p.status} /></td>
                  <td className="px-4 py-3 text-green-600">৳{((p.adminProfit || 0) / 100).toFixed(2)}</td>
                  <td className="px-4 py-3 text-xs text-gray-500">
                    {p.createdAt ? format(new Date(p.createdAt), 'dd MMM yy HH:mm') : '—'}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
        <div className="px-4 pb-4">
          <Pagination page={page} pages={pagination.pages} onChange={setPage} />
        </div>
      </div>
    </div>
  );
}
