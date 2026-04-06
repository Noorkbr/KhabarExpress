import React, { useEffect, useState, useCallback } from 'react';
import api from '../services/api';
import { PageSpinner } from '../components/shared/Spinner';
import StatusBadge from '../components/shared/StatusBadge';
import Pagination from '../components/shared/Pagination';
import Modal from '../components/shared/Modal';
import ExportButton, { exportToCSV } from '../components/shared/ExportButton';
import toast from 'react-hot-toast';
import { FiSearch, FiEye } from 'react-icons/fi';
import { format } from 'date-fns';

const ORDER_STATUSES = ['pending', 'confirmed', 'preparing', 'ready', 'picked_up', 'on_the_way', 'delivered', 'cancelled'];
const PAYMENT_METHODS = ['bkash', 'nagad', 'rocket', 'upay', 'card', 'cod'];

export default function OrdersPage() {
  const [orders, setOrders] = useState([]);
  const [pagination, setPagination] = useState({ page: 1, pages: 1, total: 0 });
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState({ status: '', paymentMethod: '', search: '', startDate: '', endDate: '' });
  const [page, setPage] = useState(1);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [statusModal, setStatusModal] = useState(null);
  const [newStatus, setNewStatus] = useState('');
  const [cancelReason, setCancelReason] = useState('');
  const [updating, setUpdating] = useState(false);

  const fetchOrders = useCallback(async () => {
    setLoading(true);
    try {
      const params = new URLSearchParams({ page, limit: 20, ...filters });
      Object.keys(filters).forEach((k) => !filters[k] && params.delete(k));
      const { data } = await api.get(`/admin/orders?${params}`);
      setOrders(data.data || []);
      setPagination(data.pagination || {});
    } catch {
      toast.error('Failed to load orders');
    } finally {
      setLoading(false);
    }
  }, [page, filters]);

  useEffect(() => { fetchOrders(); }, [fetchOrders]);

  const handleFilterChange = (e) => {
    setFilters((f) => ({ ...f, [e.target.name]: e.target.value }));
    setPage(1);
  };

  const handleUpdateStatus = async () => {
    setUpdating(true);
    try {
      await api.patch(`/admin/orders/${statusModal._id}/status`, {
        status: newStatus,
        cancellationReason: newStatus === 'cancelled' ? cancelReason : undefined,
      });
      toast.success('Order status updated');
      setStatusModal(null);
      fetchOrders();
    } catch {
      toast.error('Failed to update status');
    } finally {
      setUpdating(false);
    }
  };

  const handleExport = () => {
    exportToCSV('orders', orders.map((o) => ({
      'Order #': o.orderNumber,
      Customer: o.user?.name || '',
      Phone: o.user?.phone || '',
      Restaurant: o.restaurant?.name || '',
      Amount: `৳${((o.total || 0) / 100).toFixed(2)}`,
      Status: o.status,
      'Payment Method': o.paymentMethod,
      Date: o.createdAt ? format(new Date(o.createdAt), 'yyyy-MM-dd HH:mm') : '',
    })));
  };

  return (
    <div className="space-y-4">
      {/* Filters */}
      <div className="card flex flex-wrap gap-3">
        <div className="relative flex-1 min-w-48">
          <FiSearch className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
          <input
            name="search"
            value={filters.search}
            onChange={handleFilterChange}
            placeholder="Order # or customer..."
            className="input pl-9"
          />
        </div>
        <select name="status" value={filters.status} onChange={handleFilterChange} className="input w-40">
          <option value="">All Statuses</option>
          {ORDER_STATUSES.map((s) => <option key={s} value={s}>{s.replace(/_/g, ' ')}</option>)}
        </select>
        <select name="paymentMethod" value={filters.paymentMethod} onChange={handleFilterChange} className="input w-40">
          <option value="">All Methods</option>
          {PAYMENT_METHODS.map((m) => <option key={m} value={m}>{m}</option>)}
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
              {['Order #', 'Customer', 'Restaurant', 'Amount', 'Status', 'Payment', 'Date', 'Actions'].map((h) => (
                <th key={h} className="px-4 py-3 font-medium">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan={8} className="text-center py-12"><PageSpinner /></td></tr>
            ) : orders.length === 0 ? (
              <tr><td colSpan={8} className="text-center py-12 text-gray-400">No orders found</td></tr>
            ) : (
              orders.map((o) => (
                <tr key={o._id} className="border-b border-gray-50 hover:bg-gray-50">
                  <td className="px-4 py-3 font-mono text-xs">{o.orderNumber}</td>
                  <td className="px-4 py-3">
                    <div>{o.user?.name || '—'}</div>
                    <div className="text-xs text-gray-400">{o.user?.phone}</div>
                  </td>
                  <td className="px-4 py-3">{o.restaurant?.name || '—'}</td>
                  <td className="px-4 py-3 font-medium">৳{((o.total || 0) / 100).toFixed(2)}</td>
                  <td className="px-4 py-3"><StatusBadge status={o.status} /></td>
                  <td className="px-4 py-3 capitalize">{o.paymentMethod}</td>
                  <td className="px-4 py-3 text-xs text-gray-500">
                    {o.createdAt ? format(new Date(o.createdAt), 'dd MMM yy') : '—'}
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex gap-2">
                      <button onClick={() => setSelectedOrder(o)} className="text-blue-500 hover:text-blue-700">
                        <FiEye />
                      </button>
                      <button
                        onClick={() => { setStatusModal(o); setNewStatus(o.status); }}
                        className="text-xs text-gold hover:text-gold-600 font-medium"
                      >
                        Update
                      </button>
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

      {/* Order Detail Modal */}
      <Modal isOpen={!!selectedOrder} onClose={() => setSelectedOrder(null)} title={`Order ${selectedOrder?.orderNumber}`} size="lg">
        {selectedOrder && (
          <div className="space-y-4 text-sm">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <p className="text-gray-500">Customer</p>
                <p className="font-medium">{selectedOrder.user?.name}</p>
                <p className="text-gray-400">{selectedOrder.user?.phone}</p>
              </div>
              <div>
                <p className="text-gray-500">Restaurant</p>
                <p className="font-medium">{selectedOrder.restaurant?.name}</p>
              </div>
              <div>
                <p className="text-gray-500">Status</p>
                <StatusBadge status={selectedOrder.status} />
              </div>
              <div>
                <p className="text-gray-500">Amount</p>
                <p className="font-medium">৳{((selectedOrder.total || 0) / 100).toFixed(2)}</p>
              </div>
              <div>
                <p className="text-gray-500">Payment</p>
                <p className="font-medium capitalize">{selectedOrder.paymentMethod}</p>
              </div>
              <div>
                <p className="text-gray-500">Date</p>
                <p className="font-medium">
                  {selectedOrder.createdAt ? format(new Date(selectedOrder.createdAt), 'dd MMM yyyy HH:mm') : '—'}
                </p>
              </div>
            </div>
            {selectedOrder.items?.length > 0 && (
              <div>
                <p className="text-gray-500 mb-2">Items</p>
                <div className="space-y-1">
                  {selectedOrder.items.map((item, i) => (
                    <div key={i} className="flex justify-between text-sm">
                      <span>{item.name} × {item.quantity}</span>
                      <span>৳{((item.price || 0) / 100).toFixed(2)}</span>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}
      </Modal>

      {/* Update Status Modal */}
      <Modal isOpen={!!statusModal} onClose={() => setStatusModal(null)} title="Update Order Status">
        {statusModal && (
          <div className="space-y-4">
            <div>
              <label className="block text-sm text-gray-600 mb-1">New Status</label>
              <select value={newStatus} onChange={(e) => setNewStatus(e.target.value)} className="input">
                {ORDER_STATUSES.map((s) => <option key={s} value={s}>{s.replace(/_/g, ' ')}</option>)}
              </select>
            </div>
            {newStatus === 'cancelled' && (
              <div>
                <label className="block text-sm text-gray-600 mb-1">Cancellation Reason</label>
                <textarea
                  value={cancelReason}
                  onChange={(e) => setCancelReason(e.target.value)}
                  className="input h-24 resize-none"
                  placeholder="Reason for cancellation..."
                />
              </div>
            )}
            <div className="flex gap-3 justify-end">
              <button onClick={() => setStatusModal(null)} className="btn-secondary">Cancel</button>
              <button onClick={handleUpdateStatus} disabled={updating} className="btn-primary">
                {updating ? 'Updating…' : 'Update Status'}
              </button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
