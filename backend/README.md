# KhabarExpress Backend API

KhabarExpress is a food delivery platform for Bangladesh. This is the backend API server built with Node.js, Express, and MongoDB.

## Tech Stack

- **Runtime:** Node.js 20+
- **Framework:** Express.js
- **Database:** MongoDB (Mongoose ODM)
- **Cache:** Redis
- **Real-time:** Socket.IO
- **Authentication:** JWT + OTP-based phone verification
- **Payment Gateways:** bKash, Nagad, Rocket, Upay, SSL Commerz (cards)
- **File Storage:** AWS S3 / Firebase Storage
- **Image Processing:** Sharp
- **Logging:** Winston
- **Testing:** Jest + Supertest

## Prerequisites

- Node.js >= 20
- MongoDB >= 6.0
- Redis >= 7.0 (optional, for caching)
- npm >= 10

## Getting Started

### 1. Clone and install dependencies

```bash
cd backend
npm install
```

### 2. Configure environment variables

```bash
cp .env.example .env
```

Edit `.env` with your values. Required variables:

| Variable | Description |
|---|---|
| `DATABASE_URL` | MongoDB connection string |
| `JWT_SECRET` | Secret for signing access tokens (min 32 chars) |
| `REFRESH_TOKEN_SECRET` | Secret for signing refresh tokens (min 32 chars) |
| `PORT` | Server port (default: 3000) |

See `.env.example` for all available configuration options including payment gateways, Firebase, AWS S3, and feature flags.

### 3. Start the server

```bash
# Development (with hot reload)
npm run dev

# Production
npm start
```

The API will be available at `http://localhost:3000`.

### 4. Verify it's running

```bash
curl http://localhost:3000/health
```

## API Endpoints

| Resource | Base Path | Description |
|---|---|---|
| Auth | `/api/v1/auth` | OTP send/verify, token refresh, logout |
| Users | `/api/v1/users` | Profile, addresses, favorites |
| Restaurants | `/api/v1/restaurants` | Browse, search, register, admin approval |
| Menu Items | `/api/v1/menu-items` | Menu CRUD, availability toggle |
| Orders | `/api/v1/orders` | Create, track, cancel, restaurant management |
| Payments | `/api/v1/payments` | Create payment, callbacks, history, refunds |
| Reviews | `/api/v1/reviews` | Create, respond, admin moderation |
| Zones | `/api/v1/zones` | Delivery zone management |
| Categories | `/api/v1/categories` | Menu category management |
| Riders | `/api/v1/riders` | Rider registration, delivery management |
| Promo Codes | `/api/v1/promo-codes` | Discount code validation and management |
| Admin | `/api/v1/admin` | Dashboard, analytics, revenue reports |
| Upload | `/api/v1/upload` | Image uploads for restaurants, menus, profiles |

Full interactive API documentation is available at `/api-docs` when the server is running (Swagger UI).

## Docker Deployment

```bash
# Start with Docker Compose (includes MongoDB and Redis)
docker-compose up -d

# Stop services
docker-compose down
```

The `docker-compose.yml` includes MongoDB, Redis, and the API server.

## Database Seeding

Seed the database with sample data for development:

```bash
node src/seeds/seed.js
```

This creates sample categories, delivery zones, a test restaurant with menu items, and a promo code.

## Testing

```bash
# Run all tests
npm test

# Run tests with coverage
npx jest --coverage

# Run a specific test file
npx jest tests/auth.test.js --verbose
```

## Project Structure

```
backend/
├── src/
│   ├── app.js              # Express app setup and middleware
│   ├── server.js           # HTTP server, Socket.IO, graceful shutdown
│   ├── config/             # Database, Redis, Firebase, Socket config
│   ├── controllers/        # Request handlers (13 controllers)
│   ├── middleware/          # Auth, error handling, rate limiting, validation
│   ├── models/             # Mongoose schemas (10 models)
│   ├── routes/             # Express route definitions
│   ├── services/           # Payment gateways, OTP, notifications
│   ├── seeds/              # Database seeder scripts
│   └── utils/              # Helpers (BDT formatter, phone validator, etc.)
├── tests/                  # Jest test suites
├── Dockerfile
├── docker-compose.yml
└── package.json
```

## Environment Configuration

The application supports multiple environments through environment variables:

- **Development:** Set `NODE_ENV=development` for verbose logging and debug mode
- **Production:** Set `NODE_ENV=production` for optimized performance and minimal logging
- **Testing:** Tests use mocked services and in-memory data; set `JWT_SECRET` and `REFRESH_TOKEN_SECRET`

### Payment Gateway Setup

Each payment gateway requires its own credentials in `.env`:

- **bKash:** `BKASH_APP_KEY`, `BKASH_APP_SECRET`, `BKASH_USERNAME`, `BKASH_PASSWORD`
- **Nagad:** `NAGAD_MERCHANT_ID`, `NAGAD_PUBLIC_KEY`, `NAGAD_PRIVATE_KEY`
- **Rocket:** `ROCKET_BASE_URL`, `ROCKET_USERNAME`, `ROCKET_PASSWORD`
- **Upay:** `UPAY_BASE_URL`, `UPAY_MERCHANT_ID`, `UPAY_MERCHANT_KEY`
- **SSL Commerz:** `SSLCOMMERZ_STORE_ID`, `SSLCOMMERZ_STORE_PASSWORD`

### Feature Flags

| Flag | Default | Description |
|---|---|---|
| `ENABLE_SMS_VERIFICATION` | `true` | Enable OTP via SMS |
| `ENABLE_EMAIL_VERIFICATION` | `true` | Enable email verification |
| `ENABLE_SURGE_PRICING` | `false` | Enable dynamic delivery pricing |
| `MAINTENANCE_MODE` | `false` | Return 503 for all API requests |

## License

MIT — see [LICENSE](../LICENSE) for details.
