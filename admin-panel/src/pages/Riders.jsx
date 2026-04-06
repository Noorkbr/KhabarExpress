import React, { useEffect, useState, useCallback } from 'react';
import api from '../services/api';
import { PageSpinner } from '../components/shared/Spinner';
import StatusBadge from '../components/shared/StatusBadge';
import Pagination from '../components/shared/Pagination';
import toast from 'react-hot-toast';
import { FiSearch } from 'react-icons/fi';
import { format } from 'date-fns';

export default function RidersPage() {
  const [riders, setRiders] = useState([]);
  const [pagination, setPagination] = useState({ page: 1, pages: 1, total: 0 });
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [page, setPage] = useState(1);

  const fetchRiders = useCallback(async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams({ page, limit: 20 });
      if (search) params.set('search', search);
      if (statusFilter) params.set('status', statusFilter);
      const { data } = await api.get(`/admin/riders?${params}`);
      setRiders(data.data || []);
      setPagination(data.pagination || {});
    } catch {
      toast.error('Failed to load riders');
    } finally {
      setLoading(false);
    }
  }, [page, search, statusFilter]);

  useEffect(() => { fetchRiders(); }, [fetchRiders]);

  return (
    <div className="space-y-4">
      <div className="card flex flex-wrap gap-3">
        <div className="relative flex-1 min-w-48">
          <FiSearch className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
          <input
            value={search}
            onChange={(e) => { setSearch(e.target.value); setPage(1); }}
            placeholder="Search by name or phone..."
            className="input pl-9"
          />
        </div>
        <select value={statusFilter} onChange={(e) => { setStatusFilter(e.target.value); setPage(1); }} className="input w-40">
          <option value="">All Statuses</option>
          <option value="available">Available</option>
          <option value="busy">Busy</option>
          <option value="offline">Offline</option>
        </select>
      </div>

      <div className="card overflow-x-auto p-0">
        <table className="w-full text-sm">
          <thead className="bg-gray-50">
            <tr className="text-left text-gray-500 border-b border-gray-100">
              {['Name', 'Phone', 'Vehicle', 'Status', 'Approved', 'Rating', 'Joined'].map((h) => (
                <th key={h} className="px-4 py-3 font-medium">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan={7} className="text-center py-12"><PageSpinner /></td></tr>
            ) : riders.length === 0 ? (
              <tr><td colSpan={7} className="text-center py-12 text-gray-400">No riders found</td></tr>
            ) : (
              riders.map((r) => (
                <tr key={r._id} className="border-b border-gray-50 hover:bg-gray-50">
                  <td className="px-4 py-3 font-medium">{r.name}</td>
                  <td className="px-4 py-3 text-gray-600">{r.phone}</td>
                  <td className="px-4 py-3 capitalize">{r.vehicleType || '—'}</td>
                  <td className="px-4 py-3"><StatusBadge status={r.status || 'offline'} /></td>
                  <td className="px-4 py-3"><StatusBadge status={String(r.isApproved)} /></td>
                  <td className="px-4 py-3">{r.rating?.average?.toFixed(1) || '—'}</td>
                  <td className="px-4 py-3 text-xs text-gray-500">
                    {r.createdAt ? format(new Date(r.createdAt), 'dd MMM yy') : '—'}
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
