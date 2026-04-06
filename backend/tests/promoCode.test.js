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
    of: jest.fn(() => ({
      to: jest.fn(() => ({
        emit: jest.fn(),
      })),
    })),
  })),
}));

// Mock User model (needed by auth middleware)
jest.mock('../src/models/User', () => {
  const mockUser = {
    _id: '507f1f77bcf86cd799439011',
    phone: '+8801712345678',
    name: 'User',
    email: null,
    profileImage: null,
    preferredLanguage: 'en',
    isVerified: true,
    role: 'customer',
    save: jest.fn().mockResolvedValue(true),
  };

  const Model = jest.fn().mockImplementation(() => mockUser);
  Model.findOne = jest.fn();
  Model.findById = jest.fn();
  Model.create = jest.fn();
  Model.schema = { path: jest.fn() };
  return Model;
});

// Mock Restaurant model (loaded transitively)
jest.mock('../src/models/Restaurant', () => {
  const Model = jest.fn();
  Model.find = jest.fn();
  Model.findById = jest.fn();
  Model.findOne = jest.fn();
  Model.countDocuments = jest.fn();
  Model.schema = { path: jest.fn() };
  return Model;
});

// Mock MenuItem model
jest.mock('../src/models/MenuItem', () => {
  const Model = jest.fn();
  Model.find = jest.fn();
  Model.findById = jest.fn();
  Model.schema = { path: jest.fn() };
  return Model;
});

// Mock Order model
jest.mock('../src/models/Order', () => {
  const Model = jest.fn();
  Model.find = jest.fn();
  Model.countDocuments = jest.fn();
  Model.schema = { path: jest.fn() };
  return Model;
});

// Mock PromoCode model
const mockPromoCodeInstance = {
  _id: '507f1f77bcf86cd799439044',
  code: 'SAVE20',
  description: '20% off on all orders',
  descriptionBn: '২০% ছাড়',
  type: 'percentage',
  value: 20,
  minOrderAmount: 50000,
  maxDiscount: 10000,
  usageLimit: { total: 100, perUser: 1 },
  usageCount: 5,
  validFrom: new Date('2024-01-01'),
  validUntil: new Date('2030-12-31'),
  applicableTo: 'all',
  restaurants: [],
  isActive: true,
  save: jest.fn().mockResolvedValue(true),
};

jest.mock('../src/models/PromoCode', () => {
  const Model = jest.fn().mockImplementation(function (data) {
    return { ...mockPromoCodeInstance, ...data, save: jest.fn().mockResolvedValue(true) };
  });
  Model.find = jest.fn();
  Model.findById = jest.fn();
  Model.findOne = jest.fn();
  Model.create = jest.fn();
  Model.findByIdAndUpdate = jest.fn();
  Model.findByIdAndDelete = jest.fn();
  Model.countDocuments = jest.fn();
  Model.schema = { path: jest.fn() };
  return Model;
});

// Mock remaining models loaded transitively by the app
jest.mock('../src/models/Category', () => {
  const Model = jest.fn();
  Model.find = jest.fn();
  Model.findById = jest.fn();
  Model.schema = { path: jest.fn() };
  return Model;
});

jest.mock('../src/models/Rider', () => {
  const Model = jest.fn();
  Model.find = jest.fn();
  Model.findById = jest.fn();
  Model.schema = { path: jest.fn() };
  return Model;
});

jest.mock('../src/models/Payment', () => {
  const Model = jest.fn();
  Model.find = jest.fn();
  Model.findById = jest.fn();
  Model.schema = { path: jest.fn() };
  return Model;
});

jest.mock('../src/models/Review', () => {
  const Model = jest.fn();
  Model.find = jest.fn();
  Model.findById = jest.fn();
  Model.schema = { path: jest.fn() };
  return Model;
});

jest.mock('../src/models/Zone', () => {
  const Model = jest.fn();
  Model.find = jest.fn();
  Model.findById = jest.fn();
  Model.schema = { path: jest.fn() };
  return Model;
});

const app = require('../src/app');
const PromoCode = require('../src/models/PromoCode');
const Order = require('../src/models/Order');
const User = require('../src/models/User');

// Set test environment variables
process.env.JWT_SECRET = 'test-jwt-secret-key-minimum-32-characters-long';
process.env.REFRESH_TOKEN_SECRET = 'test-refresh-token-secret-key-minimum-32-chars';
process.env.JWT_EXPIRES_IN = '15m';
process.env.REFRESH_TOKEN_EXPIRES_IN = '7d';

// Helper: generate a regular user JWT token
const generateUserToken = (userId = '507f1f77bcf86cd799439011') => {
  return jwt.sign({ userId }, process.env.JWT_SECRET, { expiresIn: '15m' });
};

// Helper: generate an admin JWT token
const generateAdminToken = (userId = '507f1f77bcf86cd799439033') => {
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
    then: (resolve) => resolve(resolvedValue),
    catch: jest.fn(),
  };
  return chain;
};

// Helper: mock admin user for adminAuth middleware
const mockAdminUser = () => {
  User.findById.mockResolvedValue({
    _id: '507f1f77bcf86cd799439033',
    role: 'admin',
    phone: '+8801700000000',
    name: 'Admin',
  });
};

// Helper: mock regular user for auth middleware
const mockRegularUser = () => {
  User.findById.mockResolvedValue({
    _id: '507f1f77bcf86cd799439011',
    role: 'customer',
    phone: '+8801712345678',
    name: 'User',
    userId: '507f1f77bcf86cd799439011',
  });
};

describe('PromoCode Endpoints', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  // ─── Admin: Create Promo Code ──────────────────────────────────────

  describe('POST /api/v1/promo-codes', () => {
    it('should create a new promo code as admin', async () => {
      const token = generateAdminToken();
      mockAdminUser();

      const newPromo = {
        code: 'SUMMER25',
        type: 'percentage',
        value: 25,
        validFrom: '2024-06-01T00:00:00.000Z',
        validUntil: '2030-12-31T23:59:59.000Z',
        description: 'Summer sale 25% off',
        minOrderAmount: 30000,
        maxDiscount: 15000,
      };

      PromoCode.findOne.mockResolvedValue(null);
      PromoCode.create.mockResolvedValue({
        _id: '507f1f77bcf86cd799439055',
        ...newPromo,
        code: 'SUMMER25',
        isActive: true,
      });

      const res = await request(app)
        .post('/api/v1/promo-codes')
        .set('Authorization', `Bearer ${token}`)
        .send(newPromo);

      expect(res.statusCode).toBe(201);
      expect(res.body.success).toBe(true);
      expect(res.body.message).toBe('Promo code created successfully');
    });

    it('should reject duplicate promo code', async () => {
      const token = generateAdminToken();
      mockAdminUser();

      PromoCode.findOne.mockResolvedValue(mockPromoCodeInstance);

      const res = await request(app)
        .post('/api/v1/promo-codes')
        .set('Authorization', `Bearer ${token}`)
        .send({
          code: 'SAVE20',
          type: 'percentage',
          value: 20,
          validFrom: '2024-01-01T00:00:00.000Z',
          validUntil: '2030-12-31T23:59:59.000Z',
        });

      expect(res.statusCode).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Promo code already exists');
    });

    it('should reject create with missing required fields', async () => {
      const token = generateAdminToken();
      mockAdminUser();

      const res = await request(app)
        .post('/api/v1/promo-codes')
        .set('Authorization', `Bearer ${token}`)
        .send({ code: 'INCOMPLETE' });

      expect(res.statusCode).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Validation failed');
    });
  });

  // ─── User: Validate Promo Code ─────────────────────────────────────

  describe('POST /api/v1/promo-codes/validate', () => {
    it('should validate a valid promo code and return discount', async () => {
      const token = generateUserToken();
      mockRegularUser();

      const validPromo = {
        ...mockPromoCodeInstance,
        validFrom: new Date('2020-01-01'),
        validUntil: new Date('2030-12-31'),
        isActive: true,
        usageCount: 0,
        usageLimit: { total: 100, perUser: 2 },
      };
      PromoCode.findOne.mockResolvedValue(validPromo);
      Order.countDocuments.mockResolvedValue(0);

      const res = await request(app)
        .post('/api/v1/promo-codes/validate')
        .set('Authorization', `Bearer ${token}`)
        .send({ code: 'SAVE20', restaurantId: '507f1f77bcf86cd799439022', orderAmount: 100000 });

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.message).toBe('Promo code is valid');
      expect(res.body.data).toHaveProperty('discount');
      expect(res.body.data).toHaveProperty('finalAmount');
    });

    it('should return 404 for invalid promo code', async () => {
      const token = generateUserToken();
      mockRegularUser();

      PromoCode.findOne.mockResolvedValue(null);

      const res = await request(app)
        .post('/api/v1/promo-codes/validate')
        .set('Authorization', `Bearer ${token}`)
        .send({ code: 'FAKECODE', orderAmount: 100000 });

      expect(res.statusCode).toBe(404);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Invalid promo code');
    });
  });

  // ─── Admin: Get All Promo Codes ────────────────────────────────────

  describe('GET /api/v1/promo-codes', () => {
    it('should return paginated promo codes for admin', async () => {
      const token = generateAdminToken();
      mockAdminUser();

      PromoCode.find.mockReturnValue(chainableQuery([mockPromoCodeInstance]));
      PromoCode.countDocuments.mockResolvedValue(1);

      const res = await request(app)
        .get('/api/v1/promo-codes')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data.promoCodes).toHaveLength(1);
      expect(res.body.data.pagination).toHaveProperty('total', 1);
    });
  });

  // ─── User: Get Active Promo Codes ──────────────────────────────────

  describe('GET /api/v1/promo-codes/active', () => {
    it('should return active promo codes for authenticated user', async () => {
      const token = generateUserToken();
      mockRegularUser();

      PromoCode.find.mockReturnValue(chainableQuery([mockPromoCodeInstance]));

      const res = await request(app)
        .get('/api/v1/promo-codes/active')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toHaveLength(1);
    });
  });

  // ─── Admin: Get Promo Code by ID ───────────────────────────────────

  describe('GET /api/v1/promo-codes/:promoId', () => {
    it('should return promo code by ID for admin', async () => {
      const token = generateAdminToken();
      mockAdminUser();

      PromoCode.findById.mockReturnValue(chainableQuery(mockPromoCodeInstance));

      const res = await request(app)
        .get(`/api/v1/promo-codes/${mockPromoCodeInstance._id}`)
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data.code).toBe('SAVE20');
    });

    it('should return 404 for non-existent promo code', async () => {
      const token = generateAdminToken();
      mockAdminUser();

      PromoCode.findById.mockReturnValue(chainableQuery(null));

      const res = await request(app)
        .get('/api/v1/promo-codes/507f1f77bcf86cd799439099')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(404);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Promo code not found');
    });
  });

  // ─── Admin: Update Promo Code ──────────────────────────────────────

  describe('PUT /api/v1/promo-codes/:promoId', () => {
    it('should update a promo code', async () => {
      const token = generateAdminToken();
      mockAdminUser();

      PromoCode.findByIdAndUpdate.mockResolvedValue({
        ...mockPromoCodeInstance,
        description: 'Updated description',
      });

      const res = await request(app)
        .put(`/api/v1/promo-codes/${mockPromoCodeInstance._id}`)
        .set('Authorization', `Bearer ${token}`)
        .send({ description: 'Updated description', value: 30 });

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.message).toBe('Promo code updated successfully');
    });
  });

  // ─── Admin: Toggle Promo Code Status ───────────────────────────────

  describe('PATCH /api/v1/promo-codes/:promoId/toggle-status', () => {
    it('should toggle promo code active status', async () => {
      const token = generateAdminToken();
      mockAdminUser();

      const saveMock = jest.fn().mockResolvedValue(true);
      PromoCode.findById.mockResolvedValue({
        ...mockPromoCodeInstance,
        isActive: true,
        save: saveMock,
      });

      const res = await request(app)
        .patch(`/api/v1/promo-codes/${mockPromoCodeInstance._id}/toggle-status`)
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(saveMock).toHaveBeenCalled();
    });
  });

  // ─── Admin: Delete Promo Code ──────────────────────────────────────

  describe('DELETE /api/v1/promo-codes/:promoId', () => {
    it('should delete a promo code', async () => {
      const token = generateAdminToken();
      mockAdminUser();

      PromoCode.findByIdAndDelete.mockResolvedValue(mockPromoCodeInstance);

      const res = await request(app)
        .delete(`/api/v1/promo-codes/${mockPromoCodeInstance._id}`)
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.message).toBe('Promo code deleted successfully');
    });
  });

  // ─── Admin: Promo Code Stats ───────────────────────────────────────

  describe('GET /api/v1/promo-codes/:promoId/stats', () => {
    it('should return promo code stats', async () => {
      const token = generateAdminToken();
      mockAdminUser();

      PromoCode.findById.mockResolvedValue(mockPromoCodeInstance);
      Order.find.mockReturnValue(chainableQuery([
        { discount: 5000, totalAmount: 50000, userId: '507f1f77bcf86cd799439011' },
        { discount: 3000, totalAmount: 30000, userId: '507f1f77bcf86cd799439012' },
      ]));

      const res = await request(app)
        .get(`/api/v1/promo-codes/${mockPromoCodeInstance._id}/stats`)
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data.stats).toHaveProperty('totalUsage', 2);
      expect(res.body.data.stats).toHaveProperty('totalDiscount', 8000);
      expect(res.body.data.stats).toHaveProperty('uniqueUsers', 2);
    });
  });

  // ─── Authentication Requirements ───────────────────────────────────

  describe('Authentication requirements', () => {
    it('should reject requests without token', async () => {
      const res = await request(app).get('/api/v1/promo-codes/active');

      expect(res.statusCode).toBe(401);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('No authentication token provided');
    });

    it('should reject admin routes when user is not admin', async () => {
      const token = generateUserToken();
      mockRegularUser();

      const res = await request(app)
        .get('/api/v1/promo-codes')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(403);
      expect(res.body.success).toBe(false);
    });
  });
});
