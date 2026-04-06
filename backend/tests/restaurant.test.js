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

// Mock User model
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

// Mock Restaurant model
const mockRestaurantInstance = {
  _id: '507f1f77bcf86cd799439022',
  name: 'Test Restaurant',
  nameBn: 'টেস্ট রেস্টুরেন্ট',
  phone: '+8801812345678',
  email: 'test@restaurant.com',
  location: { type: 'Point', coordinates: [90.4125, 23.8103] },
  address: { street: '123 Main St', area: 'Gulshan', thana: 'Gulshan', district: 'Dhaka' },
  cuisines: ['Bengali', 'Chinese'],
  category: 'Restaurant',
  rating: 4.5,
  totalReviews: 100,
  deliveryTime: { min: 30, max: 45 },
  minOrderAmount: 0,
  deliveryRadius: 5,
  commission: 18,
  approvalStatus: 'approved',
  isActive: true,
  isOpen: true,
  featured: true,
  save: jest.fn().mockResolvedValue(true),
};

jest.mock('../src/models/Restaurant', () => {
  const Model = jest.fn().mockImplementation(function (data) {
    return { ...mockRestaurantInstance, ...data, save: jest.fn().mockResolvedValue(true) };
  });
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
  Model.schema = { path: jest.fn() };
  return Model;
});

const app = require('../src/app');
const Restaurant = require('../src/models/Restaurant');
const MenuItem = require('../src/models/MenuItem');
const User = require('../src/models/User');

// Set test environment variables
process.env.JWT_SECRET = 'test-jwt-secret-key-minimum-32-characters-long';
process.env.REFRESH_TOKEN_SECRET = 'test-refresh-token-secret-key-minimum-32-chars';
process.env.JWT_EXPIRES_IN = '15m';
process.env.REFRESH_TOKEN_EXPIRES_IN = '7d';

// Helper: generate a restaurant owner JWT token
const generateRestaurantToken = (restaurantId = '507f1f77bcf86cd799439022') => {
  return jwt.sign({ restaurantId }, process.env.JWT_SECRET, { expiresIn: '15m' });
};

// Helper: generate an admin JWT token
const generateAdminToken = (userId = '507f1f77bcf86cd799439033') => {
  return jwt.sign({ userId }, process.env.JWT_SECRET, { expiresIn: '15m' });
};

// Helper: generate a regular user JWT token (no restaurantId, no admin role)
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
    then: (resolve) => resolve(resolvedValue),
    catch: jest.fn(),
  };
  // Make it thenable so await works
  chain[Symbol.for('jest.asymmetricMatch')] = undefined;
  return chain;
};

describe('Restaurant Endpoints', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  // ─── Public Routes ────────────────────────────────────────────────

  describe('GET /api/v1/restaurants', () => {
    it('should return a paginated list of approved restaurants', async () => {
      const restaurants = [mockRestaurantInstance];
      Restaurant.find.mockReturnValue(chainableQuery(restaurants));
      Restaurant.countDocuments.mockResolvedValue(1);

      const res = await request(app).get('/api/v1/restaurants');

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data.restaurants).toHaveLength(1);
      expect(res.body.data.pagination).toHaveProperty('total', 1);
      expect(res.body.data.pagination).toHaveProperty('page', 1);
    });

    it('should return empty list when no restaurants found', async () => {
      Restaurant.find.mockReturnValue(chainableQuery([]));
      Restaurant.countDocuments.mockResolvedValue(0);

      const res = await request(app).get('/api/v1/restaurants');

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data.restaurants).toHaveLength(0);
      expect(res.body.data.pagination.total).toBe(0);
    });
  });

  describe('GET /api/v1/restaurants/featured', () => {
    it('should return featured restaurants', async () => {
      const featured = [{ ...mockRestaurantInstance, featured: true }];
      Restaurant.find.mockReturnValue(chainableQuery(featured));

      const res = await request(app).get('/api/v1/restaurants/featured');

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data.restaurants).toHaveLength(1);
    });

    it('should return empty array when no featured restaurants', async () => {
      Restaurant.find.mockReturnValue(chainableQuery([]));

      const res = await request(app).get('/api/v1/restaurants/featured');

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data.restaurants).toHaveLength(0);
    });
  });

  describe('GET /api/v1/restaurants/search', () => {
    it('should return matching restaurants and menu items', async () => {
      Restaurant.find.mockReturnValue(chainableQuery([mockRestaurantInstance]));
      MenuItem.find.mockReturnValue(chainableQuery([]));

      const res = await request(app)
        .get('/api/v1/restaurants/search')
        .query({ query: 'Bengali' });

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toHaveProperty('restaurants');
      expect(res.body.data).toHaveProperty('menuItems');
    });

    it('should return 400 when search query is missing', async () => {
      const res = await request(app).get('/api/v1/restaurants/search');

      expect(res.statusCode).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Search query is required');
    });
  });

  describe('GET /api/v1/restaurants/:id', () => {
    it('should return restaurant by ID with grouped menu', async () => {
      Restaurant.findById.mockReturnValue(chainableQuery(mockRestaurantInstance));
      MenuItem.find.mockReturnValue(chainableQuery([
        { category: 'Rice', name: 'Biryani', restaurant: mockRestaurantInstance._id },
      ]));

      const res = await request(app)
        .get(`/api/v1/restaurants/${mockRestaurantInstance._id}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data.restaurant.name).toBe('Test Restaurant');
      expect(res.body.data).toHaveProperty('menu');
    });

    it('should return 404 for non-existent restaurant', async () => {
      Restaurant.findById.mockReturnValue(chainableQuery(null));

      const res = await request(app)
        .get('/api/v1/restaurants/507f1f77bcf86cd799439099');

      expect(res.statusCode).toBe(404);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Restaurant not found');
    });
  });

  // ─── Authentication Checks ────────────────────────────────────────

  describe('Authentication requirements', () => {
    it('should reject restaurantAuth routes without token', async () => {
      const res = await request(app).put('/api/v1/restaurants/profile');

      expect(res.statusCode).toBe(401);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('No authentication token provided');
    });

    it('should reject restaurantAuth routes with invalid token', async () => {
      const res = await request(app)
        .put('/api/v1/restaurants/profile')
        .set('Authorization', 'Bearer invalid-token');

      expect(res.statusCode).toBe(401);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Invalid token');
    });

    it('should reject restaurantAuth routes when token has no restaurantId', async () => {
      const userToken = generateUserToken();

      const res = await request(app)
        .put('/api/v1/restaurants/profile')
        .set('Authorization', `Bearer ${userToken}`);

      expect(res.statusCode).toBe(403);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Access denied. Restaurant privileges required.');
    });
  });

  // ─── Restaurant Owner Routes ──────────────────────────────────────

  describe('POST /api/v1/restaurants/register', () => {
    it('should register a new restaurant', async () => {
      const newRestaurant = {
        name: 'New Restaurant',
        phone: '+8801912345678',
        location: { type: 'Point', coordinates: [90.4125, 23.8103] },
        address: { street: '456 New St', area: 'Banani', thana: 'Banani', district: 'Dhaka' },
        cuisines: ['Bengali'],
        category: 'Restaurant',
      };

      const res = await request(app)
        .post('/api/v1/restaurants/register')
        .send(newRestaurant);

      expect(res.statusCode).toBe(201);
      expect(res.body.success).toBe(true);
      expect(res.body.message).toBe('Restaurant registered successfully. Awaiting approval.');
      expect(res.body.data).toHaveProperty('name', 'New Restaurant');
    });

    it('should reject registration with missing required fields', async () => {
      const res = await request(app)
        .post('/api/v1/restaurants/register')
        .send({ name: 'Incomplete Restaurant' });

      expect(res.statusCode).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Name, phone, location, and address are required');
    });
  });

  describe('PUT /api/v1/restaurants/profile', () => {
    it('should update restaurant profile with valid restaurant token', async () => {
      const token = generateRestaurantToken();
      Restaurant.findById.mockResolvedValue({ ...mockRestaurantInstance, save: jest.fn().mockResolvedValue(true) });

      const res = await request(app)
        .put('/api/v1/restaurants/profile')
        .set('Authorization', `Bearer ${token}`)
        .send({ name: 'Updated Restaurant', cuisines: ['Chinese', 'Thai'] });

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.message).toBe('Profile updated successfully');
    });

    it('should return 404 when restaurant not found for profile update', async () => {
      const token = generateRestaurantToken('507f1f77bcf86cd799439099');
      Restaurant.findById.mockResolvedValue(null);

      const res = await request(app)
        .put('/api/v1/restaurants/profile')
        .set('Authorization', `Bearer ${token}`)
        .send({ name: 'Ghost Restaurant' });

      expect(res.statusCode).toBe(404);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Restaurant not found');
    });
  });

  describe('PATCH /api/v1/restaurants/status', () => {
    it('should toggle restaurant open/close status', async () => {
      const token = generateRestaurantToken();
      const saveMock = jest.fn().mockResolvedValue(true);
      Restaurant.findById.mockResolvedValue({ ...mockRestaurantInstance, isOpen: true, save: saveMock });

      const res = await request(app)
        .patch('/api/v1/restaurants/status')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toHaveProperty('isOpen');
    });

    it('should return 404 when restaurant not found for status toggle', async () => {
      const token = generateRestaurantToken('507f1f77bcf86cd799439099');
      Restaurant.findById.mockResolvedValue(null);

      const res = await request(app)
        .patch('/api/v1/restaurants/status')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(404);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Restaurant not found');
    });
  });

  describe('GET /api/v1/restaurants/my/analytics', () => {
    it('should return analytics for the restaurant owner', async () => {
      const token = generateRestaurantToken();
      const Order = require('../src/models/Order');
      Order.find.mockReturnValue(chainableQuery([
        { total: 50000, deliveryFee: 5000 },
        { total: 30000, deliveryFee: 5000 },
      ]));

      const res = await request(app)
        .get('/api/v1/restaurants/my/analytics')
        .set('Authorization', `Bearer ${token}`)
        .query({ period: 'today' });

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toHaveProperty('totalOrders', 2);
      expect(res.body.data).toHaveProperty('totalRevenue');
      expect(res.body.data).toHaveProperty('averageOrderValue');
      expect(res.body.data).toHaveProperty('period', 'today');
    });
  });

  // ─── Admin Routes ─────────────────────────────────────────────────

  describe('GET /api/v1/restaurants/admin/all', () => {
    it('should return all restaurants for admin', async () => {
      const token = generateAdminToken();
      User.findById.mockResolvedValue({
        _id: '507f1f77bcf86cd799439033',
        role: 'admin',
        phone: '+8801700000000',
        name: 'Admin',
      });
      Restaurant.find.mockReturnValue(chainableQuery([mockRestaurantInstance]));
      Restaurant.countDocuments.mockResolvedValue(1);

      const res = await request(app)
        .get('/api/v1/restaurants/admin/all')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data.restaurants).toHaveLength(1);
      expect(res.body.data.pagination).toHaveProperty('total', 1);
    });

    it('should reject non-admin users', async () => {
      const token = generateUserToken();
      User.findById.mockResolvedValue({
        _id: '507f1f77bcf86cd799439011',
        role: 'customer',
        phone: '+8801712345678',
        name: 'User',
      });

      const res = await request(app)
        .get('/api/v1/restaurants/admin/all')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(403);
      expect(res.body.success).toBe(false);
    });
  });

  describe('GET /api/v1/restaurants/admin/pending', () => {
    it('should return pending restaurants for admin', async () => {
      const token = generateAdminToken();
      User.findById.mockResolvedValue({
        _id: '507f1f77bcf86cd799439033',
        role: 'admin',
        phone: '+8801700000000',
        name: 'Admin',
      });
      const pendingRestaurant = { ...mockRestaurantInstance, approvalStatus: 'pending' };
      Restaurant.find.mockReturnValue(chainableQuery([pendingRestaurant]));

      const res = await request(app)
        .get('/api/v1/restaurants/admin/pending')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toHaveLength(1);
    });
  });

  describe('PATCH /api/v1/restaurants/admin/:id/approve', () => {
    it('should approve a pending restaurant', async () => {
      const token = generateAdminToken();
      User.findById.mockResolvedValue({
        _id: '507f1f77bcf86cd799439033',
        role: 'admin',
        phone: '+8801700000000',
        name: 'Admin',
      });
      const saveMock = jest.fn().mockResolvedValue(true);
      Restaurant.findById.mockResolvedValue({ ...mockRestaurantInstance, approvalStatus: 'pending', save: saveMock });

      const res = await request(app)
        .patch(`/api/v1/restaurants/admin/${mockRestaurantInstance._id}/approve`)
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.message).toBe('Restaurant approved successfully');
    });

    it('should return 404 when approving non-existent restaurant', async () => {
      const token = generateAdminToken();
      User.findById.mockResolvedValue({
        _id: '507f1f77bcf86cd799439033',
        role: 'admin',
        phone: '+8801700000000',
        name: 'Admin',
      });
      Restaurant.findById.mockResolvedValue(null);

      const res = await request(app)
        .patch('/api/v1/restaurants/admin/507f1f77bcf86cd799439099/approve')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(404);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Restaurant not found');
    });
  });

  describe('PATCH /api/v1/restaurants/admin/:id/reject', () => {
    it('should reject a restaurant with reason', async () => {
      const token = generateAdminToken();
      User.findById.mockResolvedValue({
        _id: '507f1f77bcf86cd799439033',
        role: 'admin',
        phone: '+8801700000000',
        name: 'Admin',
      });
      const saveMock = jest.fn().mockResolvedValue(true);
      Restaurant.findById.mockResolvedValue({ ...mockRestaurantInstance, approvalStatus: 'pending', save: saveMock });

      const res = await request(app)
        .patch(`/api/v1/restaurants/admin/${mockRestaurantInstance._id}/reject`)
        .set('Authorization', `Bearer ${token}`)
        .send({ reason: 'Incomplete documentation' });

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.message).toBe('Restaurant rejected');
    });

    it('should return 404 when rejecting non-existent restaurant', async () => {
      const token = generateAdminToken();
      User.findById.mockResolvedValue({
        _id: '507f1f77bcf86cd799439033',
        role: 'admin',
        phone: '+8801700000000',
        name: 'Admin',
      });
      Restaurant.findById.mockResolvedValue(null);

      const res = await request(app)
        .patch('/api/v1/restaurants/admin/507f1f77bcf86cd799439099/reject')
        .set('Authorization', `Bearer ${token}`)
        .send({ reason: 'Not valid' });

      expect(res.statusCode).toBe(404);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Restaurant not found');
    });
  });
});
