import React, { useEffect, useState, useCallback } from 'react';
import api from '../services/api';
import { PageSpinner } from '../components/shared/Spinner';
import StatusBadge from '../components/shared/StatusBadge';
import Pagination from '../components/shared/Pagination';
import Modal from '../components/shared/Modal';
import toast from 'react-hot-toast';
import { FiSearch } from 'react-icons/fi';

const APPROVAL_STATUSES = ['pending', 'approved', 'rejected', 'suspended'];

export default function RestaurantsPage() {
  const [restaurants, setRestaurants] = useState([]);
  const [pagination, setPagination] = useState({ page: 1, pages: 1, total: 0 });
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [approvalFilter, setApprovalFilter] = useState('');
  const [page, setPage] = useState(1);
  const [actionModal, setActionModal] = useState(null);
  const [actionType, setActionType] = useState('');
  const [reason, setReason] = useState('');
  const [saving, setSaving] = useState(false);

  const fetchRestaurants = useCallback(async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams({ page, limit: 20 });
      if (search) params.set('search', search);
      if (approvalFilter) params.set('approvalStatus', approvalFilter);
      const { data } = await api.get(`/admin/restaurants?${params}`);
      setRestaurants(data.data || []);
      setPagination(data.pagination || {});
    } catch {
      toast.error('Failed to load restaurants');
    } finally {
      setLoading(false);
    }
  }, [page, search, approvalFilter]);

  useEffect(() => { fetchRestaurants(); }, [fetchRestaurants]);

  const openAction = (restaurant, type) => {
    setActionModal(restaurant);
    setActionType(type);
    setReason('');
  };

  const handleAction = async () => {
    setSaving(true);
    try {
      const update = {};
      if (actionType === 'approve') { update.approvalStatus = 'approved'; update.isActive = true; }
      if (actionType === 'reject') { update.approvalStatus = 'rejected'; update.rejectionReason = reason; }
      if (actionType === 'suspend') { update.isActive = false; update.rejectionReason = reason; }
      if (actionType === 'unsuspend') { update.isActive = true; update.approvalStatus = 'approved'; }

      await api.patch(`/admin/restaurants/${actionModal._id}/status`, update);
      toast.success('Restaurant updated');
      setActionModal(null);
      fetchRestaurants();
    } catch {
      toast.error('Failed to update restaurant');
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
        <select value={approvalFilter} onChange={(e) => { setApprovalFilter(e.target.value); setPage(1); }} className="input w-44">
          <option value="">All Statuses</option>
          {APPROVAL_STATUSES.map((s) => <option key={s} value={s}>{s}</option>)}
        </select>
      </div>

      <div className="card overflow-x-auto p-0">
        <table className="w-full text-sm">
          <thead className="bg-gray-50">
            <tr className="text-left text-gray-500 border-b border-gray-100">
              {['Name', 'Phone', 'Category', 'Status', 'Active', 'Joined', 'Actions'].map((h) => (
                <th key={h} className="px-4 py-3 font-medium">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan={7} className="text-center py-12"><PageSpinner /></td></tr>
            ) : restaurants.length === 0 ? (
              <tr><td colSpan={7} className="text-center py-12 text-gray-400">No restaurants found</td></tr>
            ) : (
              restaurants.map((r) => (
                <tr key={r._id} className="border-b border-gray-50 hover:bg-gray-50">
                  <td className="px-4 py-3">
                    <div className="font-medium">{r.name}</div>
                    {r.nameBn && <div className="text-xs text-gray-400">{r.nameBn}</div>}
                  </td>
                  <td className="px-4 py-3 text-gray-600">{r.phone}</td>
                  <td className="px-4 py-3">{r.category}</td>
                  <td className="px-4 py-3"><StatusBadge status={r.approvalStatus} /></td>
                  <td className="px-4 py-3"><StatusBadge status={String(r.isActive)} /></td>
                  <td className="px-4 py-3 text-xs text-gray-500">
                    {r.createdAt ? new Date(r.createdAt).toLocaleDateString() : '—'}
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex gap-2 flex-wrap">
                      {r.approvalStatus === 'pending' && (
                        <>
                          <button onClick={() => openAction(r, 'approve')} className="text-xs text-green-600 hover:underline font-medium">Approve</button>
                          <button onClick={() => openAction(r, 'reject')} className="text-xs text-red-500 hover:underline font-medium">Reject</button>
                        </>
                      )}
                      {r.approvalStatus === 'approved' && r.isActive && (
                        <button onClick={() => openAction(r, 'suspend')} className="text-xs text-orange-500 hover:underline font-medium">Suspend</button>
                      )}
                      {!r.isActive && (
                        <button onClick={() => openAction(r, 'unsuspend')} className="text-xs text-blue-500 hover:underline font-medium">Unsuspend</button>
                      )}
                    </div>
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

      <Modal isOpen={!!actionModal} onClose={() => setActionModal(null)} title={`${actionType} Restaurant`}>
        {actionModal && (
          <div className="space-y-4">
            <p className="text-sm text-gray-600">
              Are you sure you want to <strong>{actionType}</strong> <strong>{actionModal.name}</strong>?
            </p>
            {(actionType === 'reject' || actionType === 'suspend') && (
              <div>
                <label className="block text-sm text-gray-600 mb-1">Reason</label>
                <textarea
                  value={reason}
                  onChange={(e) => setReason(e.target.value)}
                  className="input h-24 resize-none"
                  placeholder="Enter reason..."
                />
              </div>
            )}
            <div className="flex gap-3 justify-end">
              <button onClick={() => setActionModal(null)} className="btn-secondary">Cancel</button>
              <button onClick={handleAction} disabled={saving} className="btn-primary">
                {saving ? 'Saving…' : 'Confirm'}
              </button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
