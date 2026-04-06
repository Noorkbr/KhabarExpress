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
  const Model = jest.fn().mockImplementation(function (data) {
    return {
      ...data,
      _id: '507f1f77bcf86cd799439044',
      save: jest.fn().mockResolvedValue(true),
      deleteOne: jest.fn().mockResolvedValue(true),
    };
  });
  Model.find = jest.fn();
  Model.findById = jest.fn();
  Model.findOne = jest.fn();
  Model.schema = { path: jest.fn() };
  return Model;
});

// Mock Category model
jest.mock('../src/models/Category', () => {
  const Model = jest.fn();
  Model.findOne = jest.fn();
  Model.find = jest.fn();
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
const MenuItem = require('../src/models/MenuItem');
const Category = require('../src/models/Category');

// Set test environment variables
process.env.JWT_SECRET = 'test-jwt-secret-key-minimum-32-characters-long';
process.env.REFRESH_TOKEN_SECRET = 'test-refresh-token-secret-key-minimum-32-chars';
process.env.JWT_EXPIRES_IN = '15m';
process.env.REFRESH_TOKEN_EXPIRES_IN = '7d';

// Helper: generate a restaurant owner JWT token
const generateRestaurantToken = (restaurantId = '507f1f77bcf86cd799439022') => {
  return jwt.sign({ restaurantId }, process.env.JWT_SECRET, { expiresIn: '15m' });
};

// Helper: generate a regular user JWT token (no restaurantId)
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
  return chain;
};

// Mock menu item data
const mockMenuItem = {
  _id: '507f1f77bcf86cd799439044',
  restaurant: '507f1f77bcf86cd799439022',
  name: 'Chicken Biryani',
  nameBn: 'চিকেন বিরিয়ানি',
  description: 'Delicious chicken biryani',
  descriptionBn: 'সুস্বাদু চিকেন বিরিয়ানি',
  image: 'https://example.com/biryani.jpg',
  category: '507f1f77bcf86cd799439055',
  categoryName: 'Rice',
  price: 25000,
  discountPrice: 22000,
  isVegetarian: false,
  isVegan: false,
  isHalal: true,
  spiceLevel: 'medium',
  allergens: [],
  customizations: [],
  prepTime: 30,
  rating: 4.5,
  totalReviews: 50,
  totalOrders: 200,
  isAvailable: true,
  isPopular: true,
};

describe('MenuItem Endpoints', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  // ─── Public Routes ────────────────────────────────────────────────

  describe('GET /api/v1/menu-items/restaurant/:restaurantId', () => {
    it('should return menu items for a restaurant', async () => {
      MenuItem.find.mockReturnValue(chainableQuery([mockMenuItem]));

      const res = await request(app)
        .get(`/api/v1/menu-items/restaurant/${mockMenuItem.restaurant}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toHaveLength(1);
      expect(res.body.data[0].name).toBe('Chicken Biryani');
    });

    it('should return empty array when no menu items found', async () => {
      MenuItem.find.mockReturnValue(chainableQuery([]));

      const res = await request(app)
        .get('/api/v1/menu-items/restaurant/507f1f77bcf86cd799439099');

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toHaveLength(0);
    });

    it('should filter menu items by category when categoryId is provided', async () => {
      MenuItem.find.mockReturnValue(chainableQuery([mockMenuItem]));

      const res = await request(app)
        .get(`/api/v1/menu-items/restaurant/${mockMenuItem.restaurant}`)
        .query({ categoryId: '507f1f77bcf86cd799439055' });

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(MenuItem.find).toHaveBeenCalledWith(
        expect.objectContaining({ category: '507f1f77bcf86cd799439055' })
      );
    });
  });

  describe('GET /api/v1/menu-items/:id', () => {
    it('should return a single menu item by ID', async () => {
      MenuItem.findById.mockReturnValue(chainableQuery(mockMenuItem));

      const res = await request(app)
        .get(`/api/v1/menu-items/${mockMenuItem._id}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data.name).toBe('Chicken Biryani');
      expect(res.body.data.price).toBe(25000);
    });

    it('should return 404 for non-existent menu item', async () => {
      MenuItem.findById.mockReturnValue(chainableQuery(null));

      const res = await request(app)
        .get('/api/v1/menu-items/507f1f77bcf86cd799439099');

      expect(res.statusCode).toBe(404);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Menu item not found');
    });
  });

  // ─── Authentication Checks ────────────────────────────────────────

  describe('Authentication requirements', () => {
    it('should reject restaurantAuth routes without token', async () => {
      const res = await request(app)
        .post('/api/v1/menu-items')
        .send({ name: 'Test', category: '507f1f77bcf86cd799439055', price: 10000 });

      expect(res.statusCode).toBe(401);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('No authentication token provided');
    });

    it('should reject restaurantAuth routes with invalid token', async () => {
      const res = await request(app)
        .post('/api/v1/menu-items')
        .set('Authorization', 'Bearer invalid-token')
        .send({ name: 'Test', category: '507f1f77bcf86cd799439055', price: 10000 });

      expect(res.statusCode).toBe(401);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Invalid token');
    });

    it('should reject restaurantAuth routes when token has no restaurantId', async () => {
      const userToken = generateUserToken();

      const res = await request(app)
        .post('/api/v1/menu-items')
        .set('Authorization', `Bearer ${userToken}`)
        .send({ name: 'Test', category: '507f1f77bcf86cd799439055', price: 10000 });

      expect(res.statusCode).toBe(403);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Access denied. Restaurant privileges required.');
    });
  });

  // ─── Restaurant Owner Routes ──────────────────────────────────────

  describe('POST /api/v1/menu-items', () => {
    it('should create a menu item with valid data', async () => {
      const token = generateRestaurantToken();
      Category.findOne.mockResolvedValue({
        _id: '507f1f77bcf86cd799439055',
        name: 'Rice',
        restaurant: '507f1f77bcf86cd799439022',
      });

      const res = await request(app)
        .post('/api/v1/menu-items')
        .set('Authorization', `Bearer ${token}`)
        .send({
          name: 'Chicken Biryani',
          category: '507f1f77bcf86cd799439055',
          price: 25000,
          description: 'Delicious biryani',
        });

      expect(res.statusCode).toBe(201);
      expect(res.body.success).toBe(true);
      expect(res.body.message).toBe('Menu item created successfully');
      expect(res.body.data).toHaveProperty('name', 'Chicken Biryani');
    });

    it('should return 400 when required fields are missing', async () => {
      const token = generateRestaurantToken();

      const res = await request(app)
        .post('/api/v1/menu-items')
        .set('Authorization', `Bearer ${token}`)
        .send({ name: 'Biryani' });

      expect(res.statusCode).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Name, category, and price are required');
    });

    it('should return 400 when category is invalid', async () => {
      const token = generateRestaurantToken();
      Category.findOne.mockResolvedValue(null);

      const res = await request(app)
        .post('/api/v1/menu-items')
        .set('Authorization', `Bearer ${token}`)
        .send({
          name: 'Chicken Biryani',
          category: '507f1f77bcf86cd799439099',
          price: 25000,
        });

      expect(res.statusCode).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Invalid category');
    });
  });

  describe('PUT /api/v1/menu-items/:id', () => {
    it('should update a menu item', async () => {
      const token = generateRestaurantToken();
      const saveMock = jest.fn().mockResolvedValue(true);
      MenuItem.findOne.mockResolvedValue({
        ...mockMenuItem,
        category: { toString: () => '507f1f77bcf86cd799439055' },
        save: saveMock,
      });

      const res = await request(app)
        .put(`/api/v1/menu-items/${mockMenuItem._id}`)
        .set('Authorization', `Bearer ${token}`)
        .send({ name: 'Updated Biryani', price: 28000 });

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.message).toBe('Menu item updated successfully');
      expect(saveMock).toHaveBeenCalled();
    });

    it('should return 404 when menu item not found for update', async () => {
      const token = generateRestaurantToken();
      MenuItem.findOne.mockResolvedValue(null);

      const res = await request(app)
        .put('/api/v1/menu-items/507f1f77bcf86cd799439099')
        .set('Authorization', `Bearer ${token}`)
        .send({ name: 'Ghost Item' });

      expect(res.statusCode).toBe(404);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Menu item not found');
    });

    it('should reject update with invalid new category', async () => {
      const token = generateRestaurantToken();
      MenuItem.findOne.mockResolvedValue({
        ...mockMenuItem,
        category: { toString: () => '507f1f77bcf86cd799439055' },
        save: jest.fn().mockResolvedValue(true),
      });
      Category.findOne.mockResolvedValue(null);

      const res = await request(app)
        .put(`/api/v1/menu-items/${mockMenuItem._id}`)
        .set('Authorization', `Bearer ${token}`)
        .send({ category: '507f1f77bcf86cd799439088' });

      expect(res.statusCode).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Invalid category');
    });
  });

  describe('PATCH /api/v1/menu-items/:id/availability', () => {
    it('should toggle menu item availability', async () => {
      const token = generateRestaurantToken();
      const saveMock = jest.fn().mockResolvedValue(true);
      MenuItem.findOne.mockResolvedValue({
        ...mockMenuItem,
        isAvailable: true,
        save: saveMock,
      });

      const res = await request(app)
        .patch(`/api/v1/menu-items/${mockMenuItem._id}/availability`)
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.message).toMatch(/Menu item (available|unavailable)/);
      expect(saveMock).toHaveBeenCalled();
    });

    it('should return 404 when menu item not found for availability toggle', async () => {
      const token = generateRestaurantToken();
      MenuItem.findOne.mockResolvedValue(null);

      const res = await request(app)
        .patch('/api/v1/menu-items/507f1f77bcf86cd799439099/availability')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(404);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Menu item not found');
    });
  });

  describe('DELETE /api/v1/menu-items/:id', () => {
    it('should delete a menu item', async () => {
      const token = generateRestaurantToken();
      const deleteOneMock = jest.fn().mockResolvedValue(true);
      MenuItem.findOne.mockResolvedValue({
        ...mockMenuItem,
        deleteOne: deleteOneMock,
      });

      const res = await request(app)
        .delete(`/api/v1/menu-items/${mockMenuItem._id}`)
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.message).toBe('Menu item deleted successfully');
      expect(deleteOneMock).toHaveBeenCalled();
    });

    it('should return 404 when menu item not found for deletion', async () => {
      const token = generateRestaurantToken();
      MenuItem.findOne.mockResolvedValue(null);

      const res = await request(app)
        .delete('/api/v1/menu-items/507f1f77bcf86cd799439099')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(404);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Menu item not found');
    });
  });
});
