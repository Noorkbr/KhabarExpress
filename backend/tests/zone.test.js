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

// Mock Restaurant model
const mockRestaurantInstance = {
  _id: '507f1f77bcf86cd799439022',
  name: 'Test Restaurant',
  nameBn: 'টেস্ট রেস্টুরেন্ট',
  isActive: true,
  location: { type: 'Point', coordinates: [90.4125, 23.8103] },
};

jest.mock('../src/models/Restaurant', () => {
  const Model = jest.fn();
  Model.find = jest.fn();
  Model.findById = jest.fn();
  Model.findOne = jest.fn();
  Model.countDocuments = jest.fn();
  Model.schema = { path: jest.fn() };
  return Model;
});

// Mock Zone model
const mockZoneInstance = {
  _id: '507f1f77bcf86cd799439066',
  name: 'Gulshan',
  nameBn: 'গুলশান',
  polygon: {
    type: 'Polygon',
    coordinates: [[[90.40, 23.78], [90.42, 23.78], [90.42, 23.80], [90.40, 23.80], [90.40, 23.78]]],
  },
  center: { type: 'Point', coordinates: [90.41, 23.79] },
  deliveryFee: 3000,
  perKmFee: 800,
  estimatedTime: '30-45 min',
  isActive: true,
  restaurantCount: 5,
  riderCount: 3,
  save: jest.fn().mockResolvedValue(true),
  deleteOne: jest.fn().mockResolvedValue(true),
};

jest.mock('../src/models/Zone', () => {
  const Model = jest.fn().mockImplementation(function (data) {
    return { ...mockZoneInstance, ...data, save: jest.fn().mockResolvedValue(true) };
  });
  Model.find = jest.fn();
  Model.findById = jest.fn();
  Model.findOne = jest.fn();
  Model.schema = { path: jest.fn() };
  return Model;
});

// Mock remaining models loaded transitively by the app
jest.mock('../src/models/MenuItem', () => {
  const Model = jest.fn();
  Model.find = jest.fn();
  Model.findById = jest.fn();
  Model.schema = { path: jest.fn() };
  return Model;
});

jest.mock('../src/models/Order', () => {
  const Model = jest.fn();
  Model.find = jest.fn();
  Model.schema = { path: jest.fn() };
  return Model;
});

jest.mock('../src/models/PromoCode', () => {
  const Model = jest.fn();
  Model.find = jest.fn();
  Model.findOne = jest.fn();
  Model.findById = jest.fn();
  Model.create = jest.fn();
  Model.schema = { path: jest.fn() };
  return Model;
});

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

const app = require('../src/app');
const Zone = require('../src/models/Zone');
const Restaurant = require('../src/models/Restaurant');
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
  });
};

describe('Zone Endpoints', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  // ─── Public: Get Active Zones ──────────────────────────────────────

  describe('GET /api/v1/zones', () => {
    it('should return active zones', async () => {
      Zone.find.mockReturnValue(chainableQuery([mockZoneInstance]));

      const res = await request(app).get('/api/v1/zones');

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toHaveLength(1);
      expect(res.body.data[0].name).toBe('Gulshan');
    });

    it('should return empty array when no active zones', async () => {
      Zone.find.mockReturnValue(chainableQuery([]));

      const res = await request(app).get('/api/v1/zones');

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toHaveLength(0);
    });
  });

  // ─── Public: Get Zone by ID ────────────────────────────────────────

  describe('GET /api/v1/zones/:id', () => {
    it('should return zone by ID', async () => {
      Zone.findById.mockResolvedValue(mockZoneInstance);

      const res = await request(app)
        .get(`/api/v1/zones/${mockZoneInstance._id}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data.name).toBe('Gulshan');
    });

    it('should return 404 for non-existent zone', async () => {
      Zone.findById.mockResolvedValue(null);

      const res = await request(app)
        .get('/api/v1/zones/507f1f77bcf86cd799439099');

      expect(res.statusCode).toBe(404);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Zone not found');
    });
  });

  // ─── Public: Get Restaurants in Zone ───────────────────────────────

  describe('GET /api/v1/zones/:id/restaurants', () => {
    it('should return restaurants in zone', async () => {
      Zone.findById.mockResolvedValue(mockZoneInstance);
      Restaurant.find.mockReturnValue(chainableQuery([mockRestaurantInstance]));

      const res = await request(app)
        .get(`/api/v1/zones/${mockZoneInstance._id}/restaurants`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toHaveProperty('zone');
      expect(res.body.data).toHaveProperty('restaurants');
      expect(res.body.data).toHaveProperty('count', 1);
    });

    it('should return 404 when zone not found for restaurant lookup', async () => {
      Zone.findById.mockResolvedValue(null);

      const res = await request(app)
        .get('/api/v1/zones/507f1f77bcf86cd799439099/restaurants');

      expect(res.statusCode).toBe(404);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Zone not found');
    });
  });

  // ─── Admin: Get All Zones ──────────────────────────────────────────

  describe('GET /api/v1/zones/admin/all', () => {
    it('should return all zones for admin', async () => {
      const token = generateAdminToken();
      mockAdminUser();

      Zone.find.mockReturnValue(chainableQuery([
        mockZoneInstance,
        { ...mockZoneInstance, _id: '507f1f77bcf86cd799439067', name: 'Banani', isActive: false },
      ]));

      const res = await request(app)
        .get('/api/v1/zones/admin/all')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toHaveLength(2);
    });

    it('should reject non-admin users', async () => {
      const token = generateUserToken();
      mockRegularUser();

      const res = await request(app)
        .get('/api/v1/zones/admin/all')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(403);
      expect(res.body.success).toBe(false);
    });
  });

  // ─── Admin: Create Zone ────────────────────────────────────────────

  describe('POST /api/v1/zones', () => {
    it('should create a new zone as admin', async () => {
      const token = generateAdminToken();
      mockAdminUser();

      const newZone = {
        name: 'Dhanmondi',
        nameBn: 'ধানমণ্ডি',
        polygon: {
          type: 'Polygon',
          coordinates: [[[90.37, 23.74], [90.39, 23.74], [90.39, 23.76], [90.37, 23.76], [90.37, 23.74]]],
        },
        center: { type: 'Point', coordinates: [90.38, 23.75] },
        deliveryFee: 3500,
      };

      const res = await request(app)
        .post('/api/v1/zones')
        .set('Authorization', `Bearer ${token}`)
        .send(newZone);

      expect(res.statusCode).toBe(201);
      expect(res.body.success).toBe(true);
      expect(res.body.message).toBe('Zone created successfully');
    });

    it('should reject zone creation with missing required fields', async () => {
      const token = generateAdminToken();
      mockAdminUser();

      const res = await request(app)
        .post('/api/v1/zones')
        .set('Authorization', `Bearer ${token}`)
        .send({ name: 'Incomplete Zone' });

      expect(res.statusCode).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Name, nameBn, polygon, and center are required');
    });
  });

  // ─── Admin: Update Zone ────────────────────────────────────────────

  describe('PUT /api/v1/zones/:id', () => {
    it('should update a zone', async () => {
      const token = generateAdminToken();
      mockAdminUser();

      const saveMock = jest.fn().mockResolvedValue(true);
      Zone.findById.mockResolvedValue({ ...mockZoneInstance, save: saveMock });

      const res = await request(app)
        .put(`/api/v1/zones/${mockZoneInstance._id}`)
        .set('Authorization', `Bearer ${token}`)
        .send({ name: 'Updated Gulshan', deliveryFee: 4000 });

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.message).toBe('Zone updated successfully');
      expect(saveMock).toHaveBeenCalled();
    });

    it('should return 404 when updating non-existent zone', async () => {
      const token = generateAdminToken();
      mockAdminUser();

      Zone.findById.mockResolvedValue(null);

      const res = await request(app)
        .put('/api/v1/zones/507f1f77bcf86cd799439099')
        .set('Authorization', `Bearer ${token}`)
        .send({ name: 'Ghost Zone' });

      expect(res.statusCode).toBe(404);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Zone not found');
    });
  });

  // ─── Admin: Toggle Zone Active ─────────────────────────────────────

  describe('PATCH /api/v1/zones/:id/toggle', () => {
    it('should toggle zone active status', async () => {
      const token = generateAdminToken();
      mockAdminUser();

      const saveMock = jest.fn().mockResolvedValue(true);
      Zone.findById.mockResolvedValue({ ...mockZoneInstance, isActive: true, save: saveMock });

      const res = await request(app)
        .patch(`/api/v1/zones/${mockZoneInstance._id}/toggle`)
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(saveMock).toHaveBeenCalled();
    });
  });

  // ─── Admin: Delete Zone ────────────────────────────────────────────

  describe('DELETE /api/v1/zones/:id', () => {
    it('should delete a zone with no restaurants', async () => {
      const token = generateAdminToken();
      mockAdminUser();

      const deleteOneMock = jest.fn().mockResolvedValue(true);
      Zone.findById.mockResolvedValue({ ...mockZoneInstance, deleteOne: deleteOneMock });
      Restaurant.countDocuments.mockResolvedValue(0);

      const res = await request(app)
        .delete(`/api/v1/zones/${mockZoneInstance._id}`)
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.message).toBe('Zone deleted successfully');
      expect(deleteOneMock).toHaveBeenCalled();
    });

    it('should reject deletion when restaurants exist in zone', async () => {
      const token = generateAdminToken();
      mockAdminUser();

      Zone.findById.mockResolvedValue(mockZoneInstance);
      Restaurant.countDocuments.mockResolvedValue(3);

      const res = await request(app)
        .delete(`/api/v1/zones/${mockZoneInstance._id}`)
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toContain('Cannot delete zone');
    });
  });

  // ─── Authentication Requirements ───────────────────────────────────

  describe('Authentication requirements', () => {
    it('should reject admin routes when user is not admin', async () => {
      const token = generateUserToken();
      mockRegularUser();

      const res = await request(app)
        .post('/api/v1/zones')
        .set('Authorization', `Bearer ${token}`)
        .send({ name: 'Test' });

      expect(res.statusCode).toBe(403);
      expect(res.body.success).toBe(false);
    });

    it('should allow public routes without token', async () => {
      Zone.find.mockReturnValue(chainableQuery([]));

      const res = await request(app).get('/api/v1/zones');

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
    });
  });
});
