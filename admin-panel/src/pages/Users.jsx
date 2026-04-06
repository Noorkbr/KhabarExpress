import React, { useEffect, useState, useCallback } from 'react';
import api from '../services/api';
import { PageSpinner } from '../components/shared/Spinner';
import StatusBadge from '../components/shared/StatusBadge';
import Pagination from '../components/shared/Pagination';
import Modal from '../components/shared/Modal';
import toast from 'react-hot-toast';
import { FiSearch } from 'react-icons/fi';
import { format } from 'date-fns';

export default function UsersPage() {
  const [users, setUsers] = useState([]);
  const [pagination, setPagination] = useState({ page: 1, pages: 1, total: 0 });
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [roleFilter, setRoleFilter] = useState('');
  const [page, setPage] = useState(1);
  const [actionModal, setActionModal] = useState(null);
  const [saving, setSaving] = useState(false);

  const fetchUsers = useCallback(async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams({ page, limit: 20 });
      if (search) params.set('search', search);
      if (roleFilter) params.set('role', roleFilter);
      const { data } = await api.get(`/admin/users?${params}`);
      setUsers(data.data || []);
      setPagination(data.pagination || {});
    } catch {
      toast.error('Failed to load users');
    } finally {
      setLoading(false);
    }
  }, [page, search, roleFilter]);

  useEffect(() => { fetchUsers(); }, [fetchUsers]);

  const handleUpdate = async (userId, update) => {
    setSaving(true);
    try {
      await api.patch(`/admin/users/${userId}`, update);
      toast.success('User updated');
      setActionModal(null);
      fetchUsers();
    } catch {
      toast.error('Failed to update user');
    } finally {
      setSaving(false);
    }
  };

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
        <select value={roleFilter} onChange={(e) => { setRoleFilter(e.target.value); setPage(1); }} className="input w-36">
          <option value="">All Roles</option>
          <option value="customer">Customer</option>
          <option value="admin">Admin</option>
        </select>
      </div>

      <div className="card overflow-x-auto p-0">
        <table className="w-full text-sm">
          <thead className="bg-gray-50">
            <tr className="text-left text-gray-500 border-b border-gray-100">
              {['Name', 'Phone', 'Email', 'Role', 'Status', 'Joined', 'Actions'].map((h) => (
                <th key={h} className="px-4 py-3 font-medium">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan={7} className="text-center py-12"><PageSpinner /></td></tr>
            ) : users.length === 0 ? (
              <tr><td colSpan={7} className="text-center py-12 text-gray-400">No users found</td></tr>
            ) : (
              users.map((u) => (
                <tr key={u._id} className="border-b border-gray-50 hover:bg-gray-50">
                  <td className="px-4 py-3 font-medium">{u.name}</td>
                  <td className="px-4 py-3 text-gray-600">{u.phone}</td>
                  <td className="px-4 py-3 text-gray-500">{u.email || '—'}</td>
                  <td className="px-4 py-3 capitalize">{u.role}</td>
                  <td className="px-4 py-3">
                    <StatusBadge status={u.isBanned ? 'banned' : 'active'} />
                  </td>
                  <td className="px-4 py-3 text-xs text-gray-500">
                    {u.createdAt ? format(new Date(u.createdAt), 'dd MMM yy') : '—'}
                  </td>
                  <td className="px-4 py-3">
                    <button
                      onClick={() => setActionModal(u)}
                      className="text-xs text-gold hover:text-gold-600 font-medium"
                    >
                      Manage
                    </button>
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

      <Modal isOpen={!!actionModal} onClose={() => setActionModal(null)} title={`Manage User: ${actionModal?.name}`}>
        {actionModal && (
          <div className="space-y-5">
            <div className="grid grid-cols-2 gap-4 text-sm">
              <div><span className="text-gray-500">Phone:</span> <strong>{actionModal.phone}</strong></div>
              <div><span className="text-gray-500">Role:</span> <strong>{actionModal.role}</strong></div>
              <div><span className="text-gray-500">Status:</span> <StatusBadge status={actionModal.isBanned ? 'banned' : 'active'} /></div>
            </div>
            <div className="flex flex-wrap gap-3">
              {actionModal.isBanned ? (
                <button
                  onClick={() => handleUpdate(actionModal._id, { isBanned: false })}
                  disabled={saving}
                  className="btn-primary"
                >
                  {saving ? 'Saving…' : '✓ Unban User'}
                </button>
              ) : (
                <button
                  onClick={() => handleUpdate(actionModal._id, { isBanned: true })}
                  disabled={saving}
                  className="btn-danger"
                >
                  {saving ? 'Saving…' : '⊘ Ban User'}
                </button>
              )}
              {actionModal.role !== 'admin' && (
                <button
                  onClick={() => handleUpdate(actionModal._id, { role: 'admin' })}
                  disabled={saving}
                  className="btn-secondary"
                >
                  Promote to Admin
                </button>
              )}
              {actionModal.role === 'admin' && (
                <button
                  onClick={() => handleUpdate(actionModal._id, { role: 'customer' })}
                  disabled={saving}
                  className="btn-secondary"
                >
                  Demote to Customer
                </button>
              )}
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
