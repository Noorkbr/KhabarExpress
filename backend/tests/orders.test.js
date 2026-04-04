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
  const mockSave = jest.fn();
  const Model = jest.fn().mockImplementation((data) => ({
    ...data,
    _id: '607f1f77bcf86cd799439099',
    save: mockSave,
  }));
  Model.find = jest.fn();
  Model.findById = jest.fn();
  Model.findOne = jest.fn();
  Model.countDocuments = jest.fn();
  Model.create = jest.fn();
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

describe('Order Endpoints', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    // Setup default auth mock
    User.findById.mockResolvedValue(mockAuthUser);
  });

  describe('Authentication', () => {
    it('should reject requests without auth token', async () => {
      const res = await request(app)
        .get('/api/v1/orders');

      expect(res.statusCode).toBe(401);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('No authentication token provided');
    });

    it('should reject requests with invalid auth token', async () => {
      const res = await request(app)
        .get('/api/v1/orders')
        .set('Authorization', 'Bearer invalid-token');

      expect(res.statusCode).toBe(401);
      expect(res.body.success).toBe(false);
    });

    it('should reject requests when user not found', async () => {
      User.findById.mockResolvedValue(null);
      const token = generateAuthToken();

      const res = await request(app)
        .get('/api/v1/orders')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(401);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('User not found');
    });
  });

  describe('POST /api/v1/orders', () => {
    it('should reject order creation with missing fields', async () => {
      const token = generateAuthToken();

      const res = await request(app)
        .post('/api/v1/orders')
        .set('Authorization', `Bearer ${token}`)
        .send({});

      expect(res.statusCode).toBe(400);
      expect(res.body.success).toBe(false);
    });

    it('should reject order with empty items array', async () => {
      const token = generateAuthToken();

      const res = await request(app)
        .post('/api/v1/orders')
        .set('Authorization', `Bearer ${token}`)
        .send({
          restaurantId: '607f1f77bcf86cd799439099',
          items: [],
          deliveryAddress: { area: 'Dhaka' },
          paymentMethod: 'bkash',
        });

      expect(res.statusCode).toBe(400);
      expect(res.body.success).toBe(false);
    });

    it('should reject order with invalid payment method', async () => {
      const token = generateAuthToken();

      const res = await request(app)
        .post('/api/v1/orders')
        .set('Authorization', `Bearer ${token}`)
        .send({
          restaurantId: '607f1f77bcf86cd799439099',
          items: [{ menuItemId: '507f1f77bcf86cd799439022', quantity: 1 }],
          deliveryAddress: { area: 'Dhaka' },
          paymentMethod: 'paypal', // Invalid for Bangladesh
        });

      expect(res.statusCode).toBe(400);
      expect(res.body.success).toBe(false);
    });
  });

  describe('GET /api/v1/orders', () => {
    it('should require authentication', async () => {
      const res = await request(app)
        .get('/api/v1/orders');

      expect(res.statusCode).toBe(401);
    });
  });

  describe('Restaurant Order Routes', () => {
    it('should reject restaurant routes without restaurant auth', async () => {
      const token = generateAuthToken(); // Regular user token, no restaurantId

      const res = await request(app)
        .get('/api/v1/orders/restaurant/my')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(403);
      expect(res.body.success).toBe(false);
    });

    it('should reject order accept without restaurant auth', async () => {
      const token = generateAuthToken();

      const res = await request(app)
        .patch('/api/v1/orders/607f1f77bcf86cd799439099/accept')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(403);
    });

    it('should reject status update with invalid status value', async () => {
      const restaurantToken = jwt.sign(
        { restaurantId: 'rest123' },
        process.env.JWT_SECRET,
        { expiresIn: '15m' }
      );

      const res = await request(app)
        .patch('/api/v1/orders/607f1f77bcf86cd799439099/status')
        .set('Authorization', `Bearer ${restaurantToken}`)
        .send({ status: 'invalid_status' });

      expect(res.statusCode).toBe(400);
      expect(res.body.success).toBe(false);
    });
  });
});
