import React from 'react';
import { FiChevronLeft, FiChevronRight } from 'react-icons/fi';

export default function Pagination({ page, pages, onChange }) {
  if (pages <= 1) return null;

  return (
    <div className="flex items-center gap-1 mt-4 justify-end">
      <button
        disabled={page === 1}
        onClick={() => onChange(page - 1)}
        className="p-1.5 rounded disabled:opacity-40 hover:bg-gray-100"
      >
        <FiChevronLeft />
      </button>
      {Array.from({ length: Math.min(pages, 7) }, (_, i) => {
        const p = i + 1;
        return (
          <button
            key={p}
            onClick={() => onChange(p)}
            className={`w-8 h-8 rounded text-sm font-medium transition-colors ${
              p === page
                ? 'bg-gold text-navy-900'
                : 'hover:bg-gray-100 text-gray-600'
            }`}
          >
            {p}
          </button>
        );
      })}
      <button
        disabled={page === pages}
        onClick={() => onChange(page + 1)}
        className="p-1.5 rounded disabled:opacity-40 hover:bg-gray-100"
      >
        <FiChevronRight />
      </button>
    </div>
  );
}
