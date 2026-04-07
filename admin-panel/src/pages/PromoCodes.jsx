import React, { useEffect, useState } from 'react';
import api from '../services/api';
import { PageSpinner } from '../components/shared/Spinner';
import StatusBadge from '../components/shared/StatusBadge';
import Modal from '../components/shared/Modal';
import Pagination from '../components/shared/Pagination';
import toast from 'react-hot-toast';
import { format } from 'date-fns';

const EMPTY_FORM = {
  code: '',
  discountType: 'percentage',
  discountValue: '',
  minOrderAmount: '',
  maxUses: '',
  expiresAt: '',
  isActive: true,
};

export default function PromoCodesPage() {
  const [codes, setCodes] = useState([]);
  const [pagination, setPagination] = useState({ page: 1, pages: 1, total: 0 });
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(1);
  const [modal, setModal] = useState(false);
  const [form, setForm] = useState(EMPTY_FORM);
  const [saving, setSaving] = useState(false);

  const fetchCodes = async (p = page) => {
    setLoading(true);
    try {
      const { data } = await api.get(`/promo-codes?page=${p}&limit=20`);
      setCodes(data.data || data.promoCodes || []);
      setPagination(data.pagination || {});
    } catch {
      toast.error('Failed to load promo codes');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchCodes(); }, [page]);

  const handleCreate = async (e) => {
    e.preventDefault();
    // Validate discount value
    const dv = parseFloat(form.discountValue);
    if (!dv || dv <= 0) {
      toast.error('Discount value must be greater than 0');
      return;
    }
    if (form.discountType === 'percentage' && dv > 100) {
      toast.error('Percentage discount cannot exceed 100%');
      return;
    }
    setSaving(true);
    try {
      await api.post('/promo-codes', form);
      toast.success('Promo code created');
      setModal(false);
      setForm(EMPTY_FORM);
      fetchCodes();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to create promo code');
    } finally {
      setSaving(false);
    }
  };

  const handleToggle = async (id, isActive) => {
    try {
      await api.patch(`/promo-codes/${id}`, { isActive: !isActive });
      toast.success('Promo code updated');
      fetchCodes();
    } catch {
      toast.error('Failed to update promo code');
    }
  };

  return (
    <div className="space-y-4">
      <div className="flex justify-end">
        <button onClick={() => setModal(true)} className="btn-primary">+ New Promo Code</button>
      </div>

      <div className="card overflow-x-auto p-0">
        <table className="w-full text-sm">
          <thead className="bg-gray-50">
            <tr className="text-left text-gray-500 border-b border-gray-100">
              {['Code', 'Discount', 'Type', 'Usage', 'Max Uses', 'Status', 'Expiry', 'Actions'].map((h) => (
                <th key={h} className="px-4 py-3 font-medium">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan={8} className="text-center py-12"><PageSpinner /></td></tr>
            ) : codes.length === 0 ? (
              <tr><td colSpan={8} className="text-center py-12 text-gray-400">No promo codes yet</td></tr>
            ) : (
              codes.map((c) => (
                <tr key={c._id} className="border-b border-gray-50 hover:bg-gray-50">
                  <td className="px-4 py-3 font-mono font-bold">{c.code}</td>
                  <td className="px-4 py-3">{c.discountValue}{c.discountType === 'percentage' ? '%' : ' ৳'}</td>
                  <td className="px-4 py-3 capitalize">{c.discountType}</td>
                  <td className="px-4 py-3">{c.usedCount || 0}</td>
                  <td className="px-4 py-3">{c.maxUses || '∞'}</td>
                  <td className="px-4 py-3"><StatusBadge status={c.isActive ? 'approved' : 'rejected'} /></td>
                  <td className="px-4 py-3 text-xs text-gray-500">
                    {c.expiresAt ? format(new Date(c.expiresAt), 'dd MMM yyyy') : 'Never'}
                  </td>
                  <td className="px-4 py-3">
                    <button
                      onClick={() => handleToggle(c._id, c.isActive)}
                      className={`text-xs font-medium ${c.isActive ? 'text-red-500 hover:text-red-700' : 'text-green-600 hover:text-green-700'}`}
                    >
                      {c.isActive ? 'Deactivate' : 'Activate'}
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

      <Modal isOpen={modal} onClose={() => setModal(false)} title="Create Promo Code">
        <form onSubmit={handleCreate} className="space-y-4">
          <div>
            <label className="block text-sm text-gray-600 mb-1">Code *</label>
            <input value={form.code} onChange={(e) => setForm({ ...form, code: e.target.value.toUpperCase() })} className="input" placeholder="SAVE20" required />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm text-gray-600 mb-1">Discount Type</label>
              <select value={form.discountType} onChange={(e) => setForm({ ...form, discountType: e.target.value })} className="input">
                <option value="percentage">Percentage (%)</option>
                <option value="flat">Flat Amount (৳)</option>
              </select>
            </div>
            <div>
              <label className="block text-sm text-gray-600 mb-1">Discount Value *</label>
              <input type="number" min="1" max={form.discountType === 'percentage' ? '100' : undefined} value={form.discountValue} onChange={(e) => setForm({ ...form, discountValue: e.target.value })} className="input" required />
            </div>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm text-gray-600 mb-1">Min Order (৳)</label>
              <input type="number" min="0" value={form.minOrderAmount} onChange={(e) => setForm({ ...form, minOrderAmount: e.target.value })} className="input" />
            </div>
            <div>
              <label className="block text-sm text-gray-600 mb-1">Max Uses</label>
              <input type="number" min="1" value={form.maxUses} onChange={(e) => setForm({ ...form, maxUses: e.target.value })} className="input" placeholder="Unlimited" />
            </div>
          </div>
          <div>
            <label className="block text-sm text-gray-600 mb-1">Expiry Date</label>
            <input type="date" value={form.expiresAt} onChange={(e) => setForm({ ...form, expiresAt: e.target.value })} className="input" />
          </div>
          <div className="flex gap-3 justify-end">
            <button type="button" onClick={() => setModal(false)} className="btn-secondary">Cancel</button>
            <button type="submit" disabled={saving} className="btn-primary">{saving ? 'Creating…' : 'Create Code'}</button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
