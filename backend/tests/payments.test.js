const request = require('supertest');
const jwt = require('jsonwebtoken');

// Mock Redis
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

// Mock Socket.IO
jest.mock('../src/config/socket', () => ({
  getIO: jest.fn().mockReturnValue({
    of: jest.fn().mockReturnValue({
      to: jest.fn().mockReturnValue({
        emit: jest.fn(),
      }),
      emit: jest.fn(),
    }),
  }),
}));

// Mock OTP service
jest.mock('../src/services/otpService', () => ({
  sendOTP: jest.fn(),
  verifyOTP: jest.fn(),
}));

// Mock payment services
jest.mock('../src/services/bkashService', () => ({
  createPayment: jest.fn(),
  executePayment: jest.fn(),
}));

jest.mock('../src/services/nagadService', () => ({
  createPayment: jest.fn(),
  verifyPayment: jest.fn(),
}));

jest.mock('../src/services/sslCommerzService', () => ({
  createPayment: jest.fn(),
  validatePayment: jest.fn(),
}));

jest.mock('../src/services/rocketService', () => ({
  createPayment: jest.fn(),
  verifyPayment: jest.fn(),
  refundPayment: jest.fn(),
}));

jest.mock('../src/services/upayService', () => ({
  createPayment: jest.fn(),
  verifyPayment: jest.fn(),
  refundPayment: jest.fn(),
}));

// Mock User model
jest.mock('../src/models/User', () => {
  const Model = jest.fn();
  Model.findOne = jest.fn();
  Model.findById = jest.fn();
  Model.create = jest.fn();
  Model.schema = { path: jest.fn() };
  return Model;
});

// Mock Order model
jest.mock('../src/models/Order', () => {
  const Model = jest.fn();
  Model.find = jest.fn();
  Model.findById = jest.fn();
  Model.findOne = jest.fn();
  Model.schema = { path: jest.fn() };
  return Model;
});

// Mock Payment model
jest.mock('../src/models/Payment', () => {
  const mockSave = jest.fn();
  const Model = jest.fn().mockImplementation((data) => ({
    ...data,
    _id: '707f1f77bcf86cd799439088',
    save: mockSave,
  }));
  Model.find = jest.fn();
  Model.findById = jest.fn();
  Model.findOne = jest.fn();
  Model.create = jest.fn();
  Model.countDocuments = jest.fn();
  Model.schema = { path: jest.fn() };
  return Model;
});

// Mock Restaurant model
jest.mock('../src/models/Restaurant', () => {
  const Model = jest.fn();
  Model.findById = jest.fn();
  Model.schema = { path: jest.fn() };
  return Model;
});

// Mock MenuItem model
jest.mock('../src/models/MenuItem', () => {
  const Model = jest.fn();
  Model.findById = jest.fn();
  Model.schema = { path: jest.fn() };
  return Model;
});

const app = require('../src/app');
const User = require('../src/models/User');

// Set test environment variables
process.env.JWT_SECRET = 'test-jwt-secret-key-minimum-32-characters-long';
process.env.REFRESH_TOKEN_SECRET = 'test-refresh-token-secret-key-minimum-32-chars';
process.env.ADMIN_PROFIT_RATE = '5';

// Helper to generate a valid auth token
const generateAuthToken = (userId = '507f1f77bcf86cd799439011') => {
  return jwt.sign({ userId }, process.env.JWT_SECRET, { expiresIn: '15m' });
};

const mockAuthUser = {
  _id: '507f1f77bcf86cd799439011',
  phone: '+8801712345678',
  name: 'Test User',
  role: 'customer',
  isVerified: true,
};

describe('Payment Endpoints', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    User.findById.mockResolvedValue(mockAuthUser);
  });

  describe('POST /api/v1/payments/create', () => {
    it('should require authentication', async () => {
      const res = await request(app)
        .post('/api/v1/payments/create')
        .send({ orderId: '607f1f77bcf86cd799439099', method: 'bkash' });

      expect(res.statusCode).toBe(401);
      expect(res.body.success).toBe(false);
    });

    it('should reject creation with missing order ID', async () => {
      const token = generateAuthToken();

      const res = await request(app)
        .post('/api/v1/payments/create')
        .set('Authorization', `Bearer ${token}`)
        .send({ method: 'bkash' });

      expect(res.statusCode).toBe(400);
      expect(res.body.success).toBe(false);
    });

    it('should reject creation with invalid payment method', async () => {
      const token = generateAuthToken();

      const res = await request(app)
        .post('/api/v1/payments/create')
        .set('Authorization', `Bearer ${token}`)
        .send({ orderId: '607f1f77bcf86cd799439099', method: 'paypal' });

      expect(res.statusCode).toBe(400);
      expect(res.body.success).toBe(false);
    });
  });

  describe('GET /api/v1/payments/order/:orderId', () => {
    it('should require authentication', async () => {
      const res = await request(app)
        .get('/api/v1/payments/order/607f1f77bcf86cd799439099');

      expect(res.statusCode).toBe(401);
    });
  });

  describe('GET /api/v1/payments/history', () => {
    it('should require authentication', async () => {
      const res = await request(app)
        .get('/api/v1/payments/history');

      expect(res.statusCode).toBe(401);
    });
  });

  describe('Admin Payment Routes', () => {
    it('GET /api/v1/payments should require admin role', async () => {
      const token = generateAuthToken();

      const res = await request(app)
        .get('/api/v1/payments')
        .set('Authorization', `Bearer ${token}`);

      // Regular user should get 403 (forbidden) since authorize('admin') checks role
      expect(res.statusCode).toBe(403);
      expect(res.body.success).toBe(false);
    });

    it('POST /api/v1/payments/:id/refund should require admin role', async () => {
      const token = generateAuthToken();

      const res = await request(app)
        .post('/api/v1/payments/707f1f77bcf86cd799439088/refund')
        .set('Authorization', `Bearer ${token}`)
        .send({ reason: 'Customer request' });

      expect(res.statusCode).toBe(403);
      expect(res.body.success).toBe(false);
    });
  });

  describe('Payment Callback Endpoints', () => {
    it('POST /api/v1/payments/bkash/callback should accept requests without auth', async () => {
      // Callback endpoints are public (called by payment gateways)
      // They should not return 401 (no auth required)
      const res = await request(app)
        .post('/api/v1/payments/bkash/callback')
        .send({ paymentID: 'test123', status: 'success' });

      // Should not require authentication (not 401)
      expect(res.statusCode).not.toBe(401);
    });

    it('POST /api/v1/payments/nagad/callback should accept requests without auth', async () => {
      const res = await request(app)
        .post('/api/v1/payments/nagad/callback')
        .send({ paymentRefId: 'test456' });

      expect(res.statusCode).not.toBe(401);
    });

    it('POST /api/v1/payments/sslcommerz/callback should accept requests without auth', async () => {
      const res = await request(app)
        .post('/api/v1/payments/sslcommerz/callback')
        .send({ tran_id: 'test789', status: 'VALID' });

      expect(res.statusCode).not.toBe(401);
    });

    it('POST /api/v1/payments/rocket/callback should accept requests without auth', async () => {
      const res = await request(app)
        .post('/api/v1/payments/rocket/callback')
        .send({ payment_id: 'rocket123', transaction_id: 'txn456', status: 'completed' });

      expect(res.statusCode).not.toBe(401);
    });

    it('POST /api/v1/payments/upay/callback should accept requests without auth', async () => {
      const res = await request(app)
        .post('/api/v1/payments/upay/callback')
        .send({ payment_id: 'upay123', transaction_id: 'txn789', status: 'success' });

      expect(res.statusCode).not.toBe(401);
    });
  });

  describe('Payment Method Validation', () => {
    it('should accept rocket as a valid payment method', async () => {
      const token = generateAuthToken();

      const res = await request(app)
        .post('/api/v1/payments/create')
        .set('Authorization', `Bearer ${token}`)
        .send({ orderId: '607f1f77bcf86cd799439099', method: 'rocket' });

      // Should not get a 400 validation error for the method field
      // (may get other errors like order not found, which is expected)
      if (res.statusCode === 400) {
        expect(res.body.message).not.toContain('Invalid payment method');
      }
    });

    it('should accept upay as a valid payment method', async () => {
      const token = generateAuthToken();

      const res = await request(app)
        .post('/api/v1/payments/create')
        .set('Authorization', `Bearer ${token}`)
        .send({ orderId: '607f1f77bcf86cd799439099', method: 'upay' });

      if (res.statusCode === 400) {
        expect(res.body.message).not.toContain('Invalid payment method');
      }
    });
  });
});
