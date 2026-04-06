import React, { useEffect, useState } from 'react';
import api from '../services/api';
import { PageSpinner } from '../components/shared/Spinner';
import Modal from '../components/shared/Modal';
import Pagination from '../components/shared/Pagination';
import toast from 'react-hot-toast';

const EMPTY_FORM = {
  name: '',
  area: '',
  thana: '',
  district: 'Dhaka',
  isActive: true,
};

export default function ZonesPage() {
  const [zones, setZones] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(1);
  const [pagination, setPagination] = useState({ page: 1, pages: 1 });
  const [modal, setModal] = useState(false);
  const [form, setForm] = useState(EMPTY_FORM);
  const [editId, setEditId] = useState(null);
  const [saving, setSaving] = useState(false);

  const fetchZones = async () => {
    setLoading(true);
    try {
      const { data } = await api.get(`/zones?page=${page}&limit=20`);
      setZones(data.data || data.zones || []);
      setPagination(data.pagination || {});
    } catch {
      toast.error('Failed to load zones');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchZones(); }, [page]);

  const openCreate = () => { setEditId(null); setForm(EMPTY_FORM); setModal(true); };
  const openEdit = (z) => { setEditId(z._id); setForm({ name: z.name, area: z.area || '', thana: z.thana || '', district: z.district || 'Dhaka', isActive: z.isActive !== false }); setModal(true); };

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      if (editId) {
        await api.patch(`/zones/${editId}`, form);
        toast.success('Zone updated');
      } else {
        await api.post('/zones', form);
        toast.success('Zone created');
      }
      setModal(false);
      fetchZones();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to save zone');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    if (!confirm('Delete this zone?')) return;
    try {
      await api.delete(`/zones/${id}`);
      toast.success('Zone deleted');
      fetchZones();
    } catch {
      toast.error('Failed to delete zone');
    }
  };

  return (
    <div className="space-y-4">
      <div className="flex justify-end">
        <button onClick={openCreate} className="btn-primary">+ Add Zone</button>
      </div>

      <div className="card overflow-x-auto p-0">
        <table className="w-full text-sm">
          <thead className="bg-gray-50">
            <tr className="text-left text-gray-500 border-b border-gray-100">
              {['Zone Name', 'Area', 'Thana', 'District', 'Status', 'Actions'].map((h) => (
                <th key={h} className="px-4 py-3 font-medium">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan={6} className="text-center py-12"><PageSpinner /></td></tr>
            ) : zones.length === 0 ? (
              <tr><td colSpan={6} className="text-center py-12 text-gray-400">No zones found</td></tr>
            ) : (
              zones.map((z) => (
                <tr key={z._id} className="border-b border-gray-50 hover:bg-gray-50">
                  <td className="px-4 py-3 font-medium">{z.name}</td>
                  <td className="px-4 py-3 text-gray-600">{z.area || '—'}</td>
                  <td className="px-4 py-3 text-gray-600">{z.thana || '—'}</td>
                  <td className="px-4 py-3 text-gray-600">{z.district || '—'}</td>
                  <td className="px-4 py-3">
                    <span className={`badge-${z.isActive !== false ? 'approved' : 'rejected'}`}>
                      {z.isActive !== false ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex gap-3">
                      <button onClick={() => openEdit(z)} className="text-xs text-gold hover:text-gold-600 font-medium">Edit</button>
                      <button onClick={() => handleDelete(z._id)} className="text-xs text-red-500 hover:text-red-700 font-medium">Delete</button>
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

      <Modal isOpen={modal} onClose={() => setModal(false)} title={editId ? 'Edit Zone' : 'Add Zone'}>
        <form onSubmit={handleSave} className="space-y-4">
          <div>
            <label className="block text-sm text-gray-600 mb-1">Zone Name *</label>
            <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} className="input" required />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm text-gray-600 mb-1">Area</label>
              <input value={form.area} onChange={(e) => setForm({ ...form, area: e.target.value })} className="input" />
            </div>
            <div>
              <label className="block text-sm text-gray-600 mb-1">Thana</label>
              <input value={form.thana} onChange={(e) => setForm({ ...form, thana: e.target.value })} className="input" />
            </div>
          </div>
          <div>
            <label className="block text-sm text-gray-600 mb-1">District</label>
            <input value={form.district} onChange={(e) => setForm({ ...form, district: e.target.value })} className="input" />
          </div>
          <div className="flex items-center gap-2">
            <input type="checkbox" id="zoneActive" checked={form.isActive} onChange={(e) => setForm({ ...form, isActive: e.target.checked })} className="rounded" />
            <label htmlFor="zoneActive" className="text-sm text-gray-600">Active</label>
          </div>
          <div className="flex gap-3 justify-end">
            <button type="button" onClick={() => setModal(false)} className="btn-secondary">Cancel</button>
            <button type="submit" disabled={saving} className="btn-primary">{saving ? 'Saving…' : 'Save Zone'}</button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
