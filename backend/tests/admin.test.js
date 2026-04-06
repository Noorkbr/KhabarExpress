const request = require('supertest');
const jwt = require('jsonwebtoken');

// Mock rate limiters to prevent throttling during tests
jest.mock('../src/middleware/rateLimiter', () => ({
  otpLimiter: (req, res, next) => next(),
  authLimiter: (req, res, next) => next(),
  apiLimiter: (req, res, next) => next(),
  paymentLimiter: (req, res, next) => next(),
  uploadLimiter: (req, res, next) => next(),
  searchLimiter: (req, res, next) => next(),
}));

// Mock Redis before any module that imports it
jest.mock('../src/config/redis', () => ({
  redisClient: null,
  cache: {
    get: jest.fn(),
    set: jest.fn(),
    del: jest.fn(),
  },
}));

// Mock Firebase
jest.mock('../src/config/firebase', () => ({
  messaging: jest.fn().mockReturnValue({
    send: jest.fn(),
    sendMulticast: jest.fn(),
  }),
}));

// Mock OTP service
jest.mock('../src/services/otpService', () => ({
  sendOTP: jest.fn(),
  verifyOTP: jest.fn(),
}));

// Mock Socket.IO
jest.mock('../src/config/socket', () => ({
  getIO: jest.fn(() => ({
    to: jest.fn(() => ({
      emit: jest.fn(),
    })),
    of: jest.fn(() => ({
      to: jest.fn(() => ({
        emit: jest.fn(),
      })),
    })),
  })),
}));

// Mock User model (for auth middleware and user growth analytics)
jest.mock('../src/models/User', () => {
  const Model = jest.fn();
  Model.findById = jest.fn();
  Model.findOne = jest.fn();
  Model.countDocuments = jest.fn();
  Model.aggregate = jest.fn();
  Model.schema = { path: jest.fn() };
  return Model;
});

// Mock Restaurant model
jest.mock('../src/models/Restaurant', () => {
  const Model = jest.fn();
  Model.findById = jest.fn();
  Model.find = jest.fn();
  Model.countDocuments = jest.fn();
  Model.schema = { path: jest.fn() };
  return Model;
});

// Mock Order model
jest.mock('../src/models/Order', () => {
  const Model = jest.fn();
  Model.find = jest.fn();
  Model.findById = jest.fn();
  Model.countDocuments = jest.fn();
  Model.aggregate = jest.fn();
  Model.schema = { path: jest.fn() };
  return Model;
});

// Mock Rider model
jest.mock('../src/models/Rider', () => {
  const Model = jest.fn();
  Model.countDocuments = jest.fn();
  Model.schema = { path: jest.fn() };
  return Model;
});

// Mock Payment model
jest.mock('../src/models/Payment', () => {
  const Model = jest.fn();
  Model.find = jest.fn();
  Model.aggregate = jest.fn();
  Model.schema = { path: jest.fn() };
  return Model;
});

// Mock Review model
jest.mock('../src/models/Review', () => {
  const Model = jest.fn();
  Model.find = jest.fn();
  Model.schema = { path: jest.fn() };
  return Model;
});

const app = require('../src/app');
const User = require('../src/models/User');
const Restaurant = require('../src/models/Restaurant');
const Order = require('../src/models/Order');
const Rider = require('../src/models/Rider');
const Payment = require('../src/models/Payment');
const Review = require('../src/models/Review');

// Set test environment variables
process.env.JWT_SECRET = 'test-jwt-secret-key-minimum-32-characters-long';
process.env.REFRESH_TOKEN_SECRET = 'test-refresh-token-secret-key-minimum-32-chars';
process.env.JWT_EXPIRES_IN = '15m';
process.env.REFRESH_TOKEN_EXPIRES_IN = '7d';
process.env.ADMIN_PROFIT_RATE = '5';

// Helper: generate an admin JWT token
const generateAdminToken = (userId = '507f1f77bcf86cd799439033') => {
  return jwt.sign({ userId }, process.env.JWT_SECRET, { expiresIn: '15m' });
};

// Helper: generate a regular user JWT token (non-admin)
const generateUserToken = (userId = '507f1f77bcf86cd799439011') => {
  return jwt.sign({ userId }, process.env.JWT_SECRET, { expiresIn: '15m' });
};

// Helper: chainable query mock for Mongoose
const chainableQuery = (resolvedValue) => {
  const chain = {
    sort: jest.fn().mockReturnThis(),
    limit: jest.fn().mockReturnThis(),
    skip: jest.fn().mockReturnThis(),
    select: jest.fn().mockReturnThis(),
    populate: jest.fn().mockReturnThis(),
    lean: jest.fn().mockReturnThis(),
    then: (resolve) => resolve(resolvedValue),
    catch: jest.fn(),
  };
  return chain;
};

// Mock admin user (returned by auth middleware)
const mockAdminUser = {
  _id: '507f1f77bcf86cd799439033',
  role: 'admin',
  phone: '+8801700000000',
  name: 'Admin User',
};

describe('Admin Endpoints', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  // ─── Authentication Checks ────────────────────────────────────────

  describe('Authentication requirements', () => {
    it('should reject requests without token', async () => {
      const res = await request(app).get('/api/v1/admin/dashboard');

      expect(res.statusCode).toBe(401);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('No authentication token provided');
    });

    it('should reject non-admin users', async () => {
      const token = generateUserToken();
      User.findById.mockResolvedValue({
        _id: '507f1f77bcf86cd799439011',
        role: 'customer',
        phone: '+8801712345678',
        name: 'Customer',
      });

      const res = await request(app)
        .get('/api/v1/admin/dashboard')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(403);
      expect(res.body.success).toBe(false);
    });
  });

  // ─── Dashboard Stats ──────────────────────────────────────────────

  describe('GET /api/v1/admin/dashboard', () => {
    it('should return dashboard stats', async () => {
      const token = generateAdminToken();
      User.findById.mockResolvedValue(mockAdminUser);

      Order.countDocuments
        .mockResolvedValueOnce(25)    // todayOrders
        .mockResolvedValueOnce(1500)  // totalOrders
        .mockResolvedValueOnce(42);   // activeOrders

      Order.aggregate.mockResolvedValue([{ _id: null, total: 500000 }]);

      User.countDocuments.mockResolvedValue(200);

      Restaurant.countDocuments
        .mockResolvedValueOnce(50)   // totalRestaurants (isActive)
        .mockResolvedValueOnce(5);   // pendingRestaurants

      Rider.countDocuments
        .mockResolvedValueOnce(30)   // totalRiders (isApproved)
        .mockResolvedValueOnce(3)    // pendingRiders
        .mockResolvedValueOnce(15);  // onlineRiders

      const res = await request(app)
        .get('/api/v1/admin/dashboard')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toHaveProperty('today');
      expect(res.body.data.today).toHaveProperty('orders');
      expect(res.body.data.today).toHaveProperty('revenue');
      expect(res.body.data).toHaveProperty('totals');
      expect(res.body.data.totals).toHaveProperty('users');
      expect(res.body.data.totals).toHaveProperty('restaurants');
      expect(res.body.data.totals).toHaveProperty('riders');
      expect(res.body.data.totals).toHaveProperty('orders');
      expect(res.body.data).toHaveProperty('pending');
      expect(res.body.data).toHaveProperty('active');
    });
  });

  // ─── Recent Activities ────────────────────────────────────────────

  describe('GET /api/v1/admin/activities', () => {
    it('should return recent activities', async () => {
      const token = generateAdminToken();
      User.findById.mockResolvedValue(mockAdminUser);

      Order.find.mockReturnValue(chainableQuery([
        { orderNumber: 'KL00010001', status: 'delivered', totalAmount: 50000, createdAt: new Date() },
      ]));
      Review.find.mockReturnValue(chainableQuery([
        { foodRating: 5, review: 'Great food!', createdAt: new Date() },
      ]));

      const res = await request(app)
        .get('/api/v1/admin/activities')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toHaveProperty('orders');
      expect(res.body.data).toHaveProperty('reviews');
      expect(res.body.data.orders).toHaveLength(1);
      expect(res.body.data.reviews).toHaveLength(1);
    });
  });

  // ─── Analytics Endpoints ──────────────────────────────────────────

  describe('GET /api/v1/admin/analytics', () => {
    it('should return analytics data', async () => {
      const token = generateAdminToken();
      User.findById.mockResolvedValue(mockAdminUser);

      // ordersAnalytics, popularRestaurants, topRiders
      Order.aggregate
        .mockResolvedValueOnce([{ _id: { year: 2024, month: 1, day: 15 }, totalOrders: 10, totalRevenue: 100000 }])
        .mockResolvedValueOnce([{ _id: 'rest1', totalOrders: 50, totalRevenue: 500000, restaurant: { name: 'Top Restaurant' } }])
        .mockResolvedValueOnce([{ _id: 'rider1', totalDeliveries: 30, totalEarnings: 15000, rider: { name: 'Top Rider' } }]);

      // paymentMethods
      Payment.aggregate.mockResolvedValue([
        { _id: 'bkash', count: 100, totalAmount: 500000 },
      ]);

      const res = await request(app)
        .get('/api/v1/admin/analytics')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toHaveProperty('orders');
      expect(res.body.data).toHaveProperty('popularRestaurants');
      expect(res.body.data).toHaveProperty('topRiders');
      expect(res.body.data).toHaveProperty('paymentMethods');
    });
  });

  describe('GET /api/v1/admin/revenue', () => {
    it('should return revenue analytics', async () => {
      const token = generateAdminToken();
      User.findById.mockResolvedValue(mockAdminUser);

      Order.aggregate.mockResolvedValue([{
        _id: null,
        totalRevenue: 1000000,
        totalOrders: 200,
        deliveryFees: 50000,
        discounts: 10000,
        subtotal: 960000,
        tax: 48000,
      }]);

      Payment.aggregate.mockResolvedValue([{
        _id: null,
        totalAdminProfit: 50000,
        totalRestaurantPayout: 950000,
        totalPayments: 1000000,
        paymentCount: 200,
      }]);

      const res = await request(app)
        .get('/api/v1/admin/revenue')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toHaveProperty('totalRevenue', 1000000);
      expect(res.body.data).toHaveProperty('totalOrders', 200);
      expect(res.body.data).toHaveProperty('adminProfit', 50000);
      expect(res.body.data).toHaveProperty('restaurantPayout', 950000);
    });
  });

  describe('GET /api/v1/admin/user-growth', () => {
    it('should return user growth analytics', async () => {
      const token = generateAdminToken();
      User.findById.mockResolvedValue(mockAdminUser);

      User.aggregate.mockResolvedValue([
        { _id: { year: 2024, month: 1, day: 10 }, newUsers: 15 },
        { _id: { year: 2024, month: 1, day: 11 }, newUsers: 22 },
      ]);

      const res = await request(app)
        .get('/api/v1/admin/user-growth')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toHaveLength(2);
      expect(res.body.data[0]).toHaveProperty('newUsers', 15);
    });
  });

  describe('GET /api/v1/admin/profit', () => {
    it('should return profit analytics', async () => {
      const token = generateAdminToken();
      User.findById.mockResolvedValue(mockAdminUser);

      // profitTimeline, profitByMethod, totals
      Payment.aggregate
        .mockResolvedValueOnce([{ _id: { year: 2024, month: 1, day: 15 }, totalAmount: 100000, adminProfit: 5000, restaurantPayout: 95000, transactionCount: 20 }])
        .mockResolvedValueOnce([{ _id: 'bkash', totalAmount: 80000, adminProfit: 4000, restaurantPayout: 76000, transactionCount: 15 }])
        .mockResolvedValueOnce([{ _id: null, totalAmount: 100000, totalAdminProfit: 5000, totalRestaurantPayout: 95000, totalTransactions: 20, avgAdminProfit: 250 }]);

      const res = await request(app)
        .get('/api/v1/admin/profit')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toHaveProperty('summary');
      expect(res.body.data).toHaveProperty('timeline');
      expect(res.body.data).toHaveProperty('byPaymentMethod');
      expect(res.body.data).toHaveProperty('adminProfitRate', 5);
    });
  });

  // ─── Verification Stats ───────────────────────────────────────────

  describe('GET /api/v1/admin/verification-stats', () => {
    it('should return restaurant verification stats', async () => {
      const token = generateAdminToken();
      User.findById.mockResolvedValue(mockAdminUser);

      Restaurant.countDocuments
        .mockResolvedValueOnce(10)   // pending
        .mockResolvedValueOnce(45)   // approved
        .mockResolvedValueOnce(5)    // rejected
        .mockResolvedValueOnce(3);   // suspended (approved + inactive)

      const res = await request(app)
        .get('/api/v1/admin/verification-stats')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toHaveProperty('pending', 10);
      expect(res.body.data).toHaveProperty('approved', 45);
      expect(res.body.data).toHaveProperty('rejected', 5);
      expect(res.body.data).toHaveProperty('suspended', 3);
      expect(res.body.data).toHaveProperty('total', 60);
    });
  });

  // ─── Data Export ──────────────────────────────────────────────────

  describe('GET /api/v1/admin/export', () => {
    it('should export data as JSON', async () => {
      const token = generateAdminToken();
      User.findById.mockResolvedValue(mockAdminUser);

      Order.find.mockReturnValue(chainableQuery([
        { _id: 'order1', orderNumber: 'KL00010001', status: 'delivered', totalAmount: 50000 },
      ]));

      const res = await request(app)
        .get('/api/v1/admin/export')
        .query({ type: 'orders' })
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toHaveLength(1);
      expect(res.body.data[0]).toHaveProperty('orderNumber', 'KL00010001');
    });

    it('should export data as CSV', async () => {
      const token = generateAdminToken();
      User.findById.mockResolvedValue(mockAdminUser);

      Order.find.mockReturnValue(chainableQuery([
        { _id: 'order1', orderNumber: 'KL00010001', status: 'delivered', totalAmount: 50000 },
      ]));

      const res = await request(app)
        .get('/api/v1/admin/export')
        .query({ type: 'orders', format: 'csv' })
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(200);
      expect(res.headers['content-type']).toMatch(/text\/csv/);
      expect(res.text).toContain('orderNumber');
    });

    it('should return 400 for invalid export type', async () => {
      const token = generateAdminToken();
      User.findById.mockResolvedValue(mockAdminUser);

      const res = await request(app)
        .get('/api/v1/admin/export')
        .query({ type: 'invalid' })
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Invalid export type');
    });
  });

  // ─── Financial Report ─────────────────────────────────────────────

  describe('GET /api/v1/admin/financial-report', () => {
    it('should export financial report as CSV', async () => {
      const token = generateAdminToken();
      User.findById.mockResolvedValue(mockAdminUser);

      Payment.find.mockReturnValue(chainableQuery([
        {
          createdAt: new Date('2024-01-15T10:30:00Z'),
          order: { orderNumber: 'KL00010001' },
          user: { name: 'Test Customer', phone: '+8801712345678' },
          method: 'bkash',
          gateway: { provider: 'bkash', transactionId: 'TXN123' },
          amount: 50000,
          adminProfitRate: 5,
          adminProfit: 2500,
          restaurantPayout: 47500,
          status: 'success',
        },
      ]));

      const res = await request(app)
        .get('/api/v1/admin/financial-report')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(200);
      expect(res.headers['content-type']).toMatch(/text\/csv/);
      expect(res.text).toContain('Date');
      expect(res.text).toContain('Admin Profit');
    });
  });

  // ─── Error Handling ───────────────────────────────────────────────

  describe('Error handling', () => {
    it('should handle database errors gracefully', async () => {
      const token = generateAdminToken();
      User.findById.mockResolvedValue(mockAdminUser);
      Order.countDocuments.mockRejectedValue(new Error('Database connection failed'));

      const res = await request(app)
        .get('/api/v1/admin/dashboard')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(500);
    });
  });
});
