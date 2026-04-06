# KhabarExpress Admin Panel

A full-featured web admin dashboard for the KhabarExpress food delivery platform, built with React + Vite + TailwindCSS.

## 🚀 Quick Start

### 1. Prerequisites
- Node.js 18+
- KhabarExpress backend running on `http://localhost:3000`

### 2. Seed the Admin User

From the repo root, run the admin seed script **once**:
```bash
cd backend && npm install
node src/seeds/seedAdmin.js
```

This creates the admin user:
- **Phone:** `+8801883688374`
- **Password:** `16741210@Noor`

### 3. Install & Run Admin Panel

```bash
cd admin-panel
npm install
cp .env.example .env
npm run dev
```

Open [http://localhost:5173](http://localhost:5173) in your browser.

### 4. Login

| Field    | Value              |
|----------|--------------------|
| Phone    | `+8801883688374`   |
| Password | `16741210@Noor`    |

---

## 🏗️ Tech Stack

| Technology     | Purpose                     |
|----------------|-----------------------------|
| React 18       | UI framework                |
| Vite           | Build tool & dev server     |
| TailwindCSS    | Styling                     |
| React Router 6 | Client-side routing         |
| Axios          | HTTP client / API calls     |
| Recharts       | Charts & graphs             |
| React Icons    | Icon library                |
| React Hot Toast| Toast notifications         |
| date-fns       | Date formatting             |

---

## 📁 Project Structure

```
admin-panel/
├── src/
│   ├── App.jsx                 # Root app with routes
│   ├── main.jsx                # Entry point
│   ├── index.css               # Tailwind + global styles
│   ├── context/
│   │   └── AuthContext.jsx     # Auth state & JWT management
│   ├── services/
│   │   └── api.js              # Axios instance
│   ├── components/
│   │   ├── layout/
│   │   │   ├── AdminLayout.jsx # Protected layout wrapper
│   │   │   ├── Sidebar.jsx     # Navigation sidebar
│   │   │   └── Header.jsx      # Top header bar
│   │   └── shared/
│   │       ├── StatusBadge.jsx # Colored status pills
│   │       ├── Modal.jsx       # Generic modal
│   │       ├── Pagination.jsx  # Page controls
│   │       ├── Spinner.jsx     # Loading indicators
│   │       └── ExportButton.jsx # CSV export helper
│   └── pages/
│       ├── Login.jsx           # Admin login page
│       ├── Dashboard.jsx       # KPIs + charts + recent orders
│       ├── Orders.jsx          # Order management
│       ├── Restaurants.jsx     # Restaurant approval/management
│       ├── Users.jsx           # User management
│       ├── Riders.jsx          # Rider management
│       ├── Payments.jsx        # Payments & accounting
│       ├── Reports.jsx         # Financial reports
│       ├── PromoCodes.jsx      # Promo code management
│       ├── Zones.jsx           # Delivery zone management
│       └── Settings.jsx        # Platform settings
├── index.html
├── vite.config.js
├── tailwind.config.js
├── postcss.config.js
├── package.json
└── .env.example
```

---

## 🎨 Brand Colors

| Name  | Hex       | Usage                   |
|-------|-----------|-------------------------|
| Navy  | `#1B2838` | Sidebar, primary backgrounds |
| Gold  | `#D4A843` | Accents, CTAs, highlights   |

---

## 🔌 API Configuration

The admin panel communicates with the KhabarExpress backend:

```env
VITE_API_URL=http://localhost:3000/api/v1
```

The Vite dev server proxies `/api` requests to the backend automatically.

All authenticated requests include a `Authorization: Bearer <token>` header.

---

## 📊 Pages

| Route           | Page              | Description                              |
|-----------------|-------------------|------------------------------------------|
| `/login`        | Login             | Admin authentication                     |
| `/`             | Dashboard         | KPIs, revenue chart, recent orders       |
| `/orders`       | Orders            | Full order management with filters       |
| `/restaurants`  | Restaurants       | Approval workflow, ban/suspend           |
| `/users`        | Users             | User management, ban/promote             |
| `/riders`       | Riders            | Rider list and status                    |
| `/payments`     | Payments          | Transaction history, profit tracking     |
| `/reports`      | Reports           | Financial reports with CSV export        |
| `/promo-codes`  | Promo Codes       | Create/manage discount codes             |
| `/zones`        | Zones             | Delivery zone management                 |
| `/settings`     | Settings          | Platform config, payment gateways        |

---

## 🔒 Security

- JWT token stored in `localStorage`
- All admin routes protected via `Authorization: Bearer` header
- Backend requires `role: 'admin'` for all `/api/v1/admin/*` endpoints
- 401 responses automatically redirect to `/login`

---

## 🏭 Production Build

```bash
npm run build
# Output in admin-panel/dist/
```

Serve the `dist/` folder with nginx or any static file server. Configure the `VITE_API_URL` to point to your production backend.
