import React from 'react';

const statusColors = {
  // Order statuses
  pending: 'badge-pending',
  confirmed: 'badge-confirmed',
  preparing: 'badge-preparing',
  ready: 'badge-confirmed',
  picked_up: 'badge-preparing',
  on_the_way: 'badge-preparing',
  delivered: 'badge-delivered',
  cancelled: 'badge-cancelled',
  // Restaurant/Approval statuses
  approved: 'badge-approved',
  rejected: 'badge-rejected',
  suspended: 'badge-suspended',
  // Payment statuses
  paid: 'badge-delivered',
  failed: 'badge-cancelled',
  refunded: 'badge-pending',
  // User
  active: 'badge-approved',
  banned: 'badge-cancelled',
  // Generic
  true: 'badge-approved',
  false: 'badge-cancelled',
};

export default function StatusBadge({ status }) {
  const cls = statusColors[status] || 'inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-700';
  return <span className={cls}>{String(status).replace(/_/g, ' ')}</span>;
}
