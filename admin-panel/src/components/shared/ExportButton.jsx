import React from 'react';
import { FiDownload } from 'react-icons/fi';

export function exportToCSV(filename, rows) {
  if (!rows || !rows.length) return;
  const keys = Object.keys(rows[0]);
  const csvContent = [
    keys.join(','),
    ...rows.map((row) =>
      keys
        .map((k) => {
          const val = row[k] == null ? '' : String(row[k]);
          return val.includes(',') || val.includes('"') || val.includes('\n')
            ? `"${val.replace(/"/g, '""')}"`
            : val;
        })
        .join(',')
    ),
  ].join('\n');

  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `${filename}.csv`;
  a.click();
  URL.revokeObjectURL(url);
}

export default function ExportButton({ onClick, label = 'Export CSV' }) {
  return (
    <button
      onClick={onClick}
      className="flex items-center gap-2 btn-secondary text-sm"
    >
      <FiDownload className="text-gold" />
      {label}
    </button>
  );
}
