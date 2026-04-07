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

// Mock Socket.IO (rider controller uses io.to().emit())
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

// Mock User model (for auth middleware)
jest.mock('../src/models/User', () => {
  const Model = jest.fn();
  Model.findById = jest.fn();
  Model.findOne = jest.fn();
  Model.schema = { path: jest.fn() };
  return Model;
});

// Mock Rider model
jest.mock('../src/models/Rider', () => {
  const Model = jest.fn();
  Model.findById = jest.fn();
  Model.findByIdAndUpdate = jest.fn();
  Model.findOne = jest.fn();
  Model.find = jest.fn();
  Model.create = jest.fn();
  Model.countDocuments = jest.fn();
  Model.schema = { path: jest.fn() };
  return Model;
});

// Mock Order model
jest.mock('../src/models/Order', () => {
  const Model = jest.fn();
  Model.findById = jest.fn();
  Model.find = jest.fn();
  Model.findOne = jest.fn();
  Model.countDocuments = jest.fn();
  Model.aggregate = jest.fn();
  Model.schema = { path: jest.fn() };
  return Model;
});

const app = require('../src/app');
const User = require('../src/models/User');
const Rider = require('../src/models/Rider');
const Order = require('../src/models/Order');

// Set test environment variables
process.env.JWT_SECRET = 'test-jwt-secret-key-minimum-32-characters-long';
process.env.REFRESH_TOKEN_SECRET = 'test-refresh-token-secret-key-minimum-32-chars';
process.env.JWT_EXPIRES_IN = '15m';
process.env.REFRESH_TOKEN_EXPIRES_IN = '7d';

// Helper: generate a rider JWT token (riderAuth expects riderId in JWT)
const generateRiderToken = (riderId = '507f1f77bcf86cd799439044') => {
  return jwt.sign({ riderId }, process.env.JWT_SECRET, { expiresIn: '15m' });
};

// Helper: generate an admin JWT token
const generateAdminToken = (userId = '507f1f77bcf86cd799439033') => {
  return jwt.sign({ userId }, process.env.JWT_SECRET, { expiresIn: '15m' });
};

// Helper: generate a regular customer JWT token (no riderId → riderAuth rejects)
const generateUserToken = (userId = '507f1f77bcf86cd799439022') => {
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

// Mock rider data
const mockRider = {
  _id: '507f1f77bcf86cd799439044',
  name: 'Test Rider',
  phone: '+8801812345678',
  vehicleType: 'motorcycle',
  nid: '1234567890',
  status: 'available',
  rating: 4.5,
  totalDeliveries: 50,
  totalReviews: 20,
  isApproved: true,
  isActive: true,
  currentLocation: { type: 'Point', coordinates: [90.4125, 23.8103] },
  save: jest.fn().mockResolvedValue(true),
};

// Mock user with rider role (returned by auth middleware)
const mockRiderUser = {
  _id: '507f1f77bcf86cd799439011',
  role: 'rider',
  riderId: '507f1f77bcf86cd799439044',
  phone: '+8801812345678',
  name: 'Test Rider',
};

describe('Rider Endpoints', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  // ─── Public Routes ────────────────────────────────────────────────

  describe('POST /api/v1/riders/register', () => {
    it('should register a new rider', async () => {
      Rider.findOne.mockResolvedValue(null);
      Rider.create.mockResolvedValue({
        _id: '507f1f77bcf86cd799439055',
        name: 'New Rider',
        phone: '+8801912345678',
        vehicleType: 'motorcycle',
        nid: '9876543210',
        isApproved: false,
      });

      const res = await request(app)
        .post('/api/v1/riders/register')
        .send({
          name: 'New Rider',
          phone: '+8801912345678',
          vehicleType: 'motorcycle',
          nid: '9876543210',
        });

      expect(res.statusCode).toBe(201);
      expect(res.body.success).toBe(true);
      expect(res.body.message).toBe('Rider registered successfully. Awaiting admin approval.');
      expect(res.body.data).toHaveProperty('name', 'New Rider');
    });

    it('should reject registration with missing required fields', async () => {
      const res = await request(app)
        .post('/api/v1/riders/register')
        .send({ name: 'Incomplete Rider' });

      expect(res.statusCode).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Validation failed');
    });

    it('should reject registration with invalid phone format', async () => {
      const res = await request(app)
        .post('/api/v1/riders/register')
        .send({
          name: 'Bad Phone Rider',
          phone: '01712345678',
          vehicleType: 'motorcycle',
          nid: '1234567890',
        });

      expect(res.statusCode).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Validation failed');
    });

    it('should reject registration when rider already exists', async () => {
      Rider.findOne.mockResolvedValue(mockRider);

      const res = await request(app)
        .post('/api/v1/riders/register')
        .send({
          name: 'Duplicate Rider',
          phone: '+8801812345678',
          vehicleType: 'motorcycle',
          nid: '1234567890',
        });

      expect(res.statusCode).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Rider with this phone number already exists');
    });

    it('should reject registration with invalid vehicle type', async () => {
      const res = await request(app)
        .post('/api/v1/riders/register')
        .send({
          name: 'Invalid Vehicle Rider',
          phone: '+8801912345678',
          vehicleType: 'truck',
          nid: '1234567890',
        });

      expect(res.statusCode).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Validation failed');
    });
  });

  // ─── Authentication Checks ────────────────────────────────────────

  describe('Authentication requirements', () => {
    it('should reject rider routes without token', async () => {
      const res = await request(app).get('/api/v1/riders/profile');

      expect(res.statusCode).toBe(401);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('No authentication token provided');
    });

    it('should reject rider routes with invalid token', async () => {
      const res = await request(app)
        .get('/api/v1/riders/profile')
        .set('Authorization', 'Bearer invalid-token');

      expect(res.statusCode).toBe(401);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Invalid token');
    });

    it('should reject rider routes when user has wrong role', async () => {
      const token = generateUserToken();
      User.findById.mockResolvedValue({
        _id: '507f1f77bcf86cd799439022',
        role: 'customer',
        phone: '+8801712345678',
        name: 'Customer',
      });

      const res = await request(app)
        .get('/api/v1/riders/profile')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(403);
      expect(res.body.success).toBe(false);
    });
  });

  // ─── Rider Profile Operations ─────────────────────────────────────

  describe('GET /api/v1/riders/profile', () => {
    it('should return rider profile', async () => {
      const token = generateRiderToken();
      Rider.findById.mockResolvedValue(mockRider);

      const res = await request(app)
        .get('/api/v1/riders/profile')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data.name).toBe('Test Rider');
    });

    it('should return 404 when rider not found', async () => {
      const token = generateRiderToken();
      Rider.findById.mockResolvedValue(null);

      const res = await request(app)
        .get('/api/v1/riders/profile')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(404);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Rider not found');
    });
  });

  describe('PUT /api/v1/riders/profile', () => {
    it('should update rider profile with allowed fields', async () => {
      const token = generateRiderToken();
      Rider.findByIdAndUpdate.mockResolvedValue({
        ...mockRider,
        name: 'Updated Rider',
        vehicleType: 'bicycle',
      });

      const res = await request(app)
        .put('/api/v1/riders/profile')
        .set('Authorization', `Bearer ${token}`)
        .send({ name: 'Updated Rider', vehicleType: 'bicycle' });

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.message).toBe('Profile updated successfully');
    });
  });

  // ─── Status and Location Updates ──────────────────────────────────

  describe('PATCH /api/v1/riders/status', () => {
    it('should update rider status to available', async () => {
      const token = generateRiderToken();
      Rider.findByIdAndUpdate.mockResolvedValue({
        ...mockRider,
        _id: mockRider._id,
        status: 'available',
      });

      const res = await request(app)
        .patch('/api/v1/riders/status')
        .set('Authorization', `Bearer ${token}`)
        .send({ status: 'available' });

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.message).toBe('Status updated successfully');
      expect(res.body.data).toHaveProperty('status', 'available');
    });

    it('should reject invalid status values', async () => {
      const token = generateRiderToken();

      const res = await request(app)
        .patch('/api/v1/riders/status')
        .set('Authorization', `Bearer ${token}`)
        .send({ status: 'invalid_status' });

      expect(res.statusCode).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Invalid status. Must be offline, available, or on_break');
    });
  });

  describe('PATCH /api/v1/riders/location', () => {
    it('should update rider location', async () => {
      const token = generateRiderToken();
      Rider.findByIdAndUpdate.mockResolvedValue(mockRider);
      Order.findOne.mockResolvedValue(null);

      const res = await request(app)
        .patch('/api/v1/riders/location')
        .set('Authorization', `Bearer ${token}`)
        .send({ latitude: 23.8103, longitude: 90.4125 });

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.message).toBe('Location updated successfully');
    });

    it('should reject location update without coordinates', async () => {
      const token = generateRiderToken();

      const res = await request(app)
        .patch('/api/v1/riders/location')
        .set('Authorization', `Bearer ${token}`)
        .send({});

      expect(res.statusCode).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Latitude and longitude are required');
    });
  });

  // ─── Order Operations ─────────────────────────────────────────────

  describe('GET /api/v1/riders/available-orders', () => {
    it('should return available orders for an available rider', async () => {
      const token = generateRiderToken();
      Rider.findById.mockResolvedValue({ ...mockRider, status: 'available' });
      Order.find.mockReturnValue(chainableQuery([
        { _id: 'order1', status: 'ready', restaurantId: 'rest1' },
        { _id: 'order2', status: 'ready', restaurantId: 'rest2' },
      ]));

      const res = await request(app)
        .get('/api/v1/riders/available-orders')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toHaveLength(2);
    });

    it('should reject if rider is not in available status', async () => {
      const token = generateRiderToken();
      Rider.findById.mockResolvedValue({ ...mockRider, status: 'offline' });

      const res = await request(app)
        .get('/api/v1/riders/available-orders')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Rider must be in available status to view orders');
    });
  });

  describe('POST /api/v1/riders/orders/:orderId/accept', () => {
    it('should accept an available order', async () => {
      const token = generateRiderToken();
      const mockOrder = {
        _id: '507f1f77bcf86cd799439066',
        status: 'ready',
        rider: null,
        save: jest.fn().mockResolvedValue(true),
      };
      Order.findById.mockResolvedValue(mockOrder);
      Rider.findByIdAndUpdate.mockResolvedValue({ ...mockRider, status: 'busy' });

      const res = await request(app)
        .post('/api/v1/riders/orders/507f1f77bcf86cd799439066/accept')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.message).toBe('Order accepted successfully');
    });

    it('should return 404 for non-existent order', async () => {
      const token = generateRiderToken();
      Order.findById.mockResolvedValue(null);

      const res = await request(app)
        .post('/api/v1/riders/orders/507f1f77bcf86cd799439099/accept')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(404);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Order not found');
    });

    it('should reject accepting an already assigned order', async () => {
      const token = generateRiderToken();
      Order.findById.mockResolvedValue({
        _id: '507f1f77bcf86cd799439066',
        status: 'ready',
        rider: '507f1f77bcf86cd799439099',
      });

      const res = await request(app)
        .post('/api/v1/riders/orders/507f1f77bcf86cd799439066/accept')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Order already assigned to a rider');
    });
  });

  describe('PATCH /api/v1/riders/orders/:orderId/status', () => {
    it('should update order status to delivered', async () => {
      const token = generateRiderToken();
      const mockOrder = {
        _id: '507f1f77bcf86cd799439066',
        status: 'picked_up',
        rider: { toString: () => '507f1f77bcf86cd799439044' },
        save: jest.fn().mockResolvedValue(true),
      };
      Order.findById.mockResolvedValue(mockOrder);
      Rider.findByIdAndUpdate.mockResolvedValue(mockRider);

      const res = await request(app)
        .patch('/api/v1/riders/orders/507f1f77bcf86cd799439066/status')
        .set('Authorization', `Bearer ${token}`)
        .send({ status: 'delivered' });

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.message).toBe('Order status updated successfully');
    });

    it('should reject invalid order status', async () => {
      const token = generateRiderToken();
      const mockOrder = {
        _id: '507f1f77bcf86cd799439066',
        status: 'picked_up',
        rider: { toString: () => '507f1f77bcf86cd799439044' },
        save: jest.fn().mockResolvedValue(true),
      };
      Order.findById.mockResolvedValue(mockOrder);

      const res = await request(app)
        .patch('/api/v1/riders/orders/507f1f77bcf86cd799439066/status')
        .set('Authorization', `Bearer ${token}`)
        .send({ status: 'cancelled' });

      expect(res.statusCode).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Invalid status');
    });

    it('should reject status update from unauthorized rider', async () => {
      const token = generateRiderToken();
      Order.findById.mockResolvedValue({
        _id: '507f1f77bcf86cd799439066',
        status: 'picked_up',
        rider: { toString: () => '507f1f77bcf86cd799439099' },
      });

      const res = await request(app)
        .patch('/api/v1/riders/orders/507f1f77bcf86cd799439066/status')
        .set('Authorization', `Bearer ${token}`)
        .send({ status: 'on_the_way' });

      expect(res.statusCode).toBe(403);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Not authorized to update this order');
    });
  });

  // ─── Earnings and Delivery History ────────────────────────────────

  describe('GET /api/v1/riders/earnings', () => {
    it('should return rider earnings summary', async () => {
      const token = generateRiderToken();
      Order.find.mockReturnValue(chainableQuery([
        { deliveryFee: 5000 },
        { deliveryFee: 6000 },
        { deliveryFee: 4500 },
      ]));

      const res = await request(app)
        .get('/api/v1/riders/earnings')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toHaveProperty('totalDeliveries', 3);
      expect(res.body.data).toHaveProperty('totalEarnings', 15500);
      expect(res.body.data).toHaveProperty('averageEarningsPerDelivery');
    });
  });

  describe('GET /api/v1/riders/delivery-history', () => {
    it('should return delivery history with pagination', async () => {
      const token = generateRiderToken();
      Order.find.mockReturnValue(chainableQuery([
        { _id: 'order1', status: 'delivered', deliveredAt: new Date() },
      ]));
      Order.countDocuments.mockResolvedValue(1);

      const res = await request(app)
        .get('/api/v1/riders/delivery-history')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toHaveProperty('orders');
      expect(res.body.data).toHaveProperty('pagination');
      expect(res.body.data.pagination).toHaveProperty('total', 1);
      expect(res.body.data.pagination).toHaveProperty('page', 1);
    });
  });

  describe('GET /api/v1/riders/stats', () => {
    it('should return rider stats including today totals', async () => {
      const token = generateRiderToken();
      Rider.findById.mockResolvedValue(mockRider);
      Order.countDocuments.mockResolvedValue(5);
      Order.aggregate.mockResolvedValue([{ _id: null, total: 25000 }]);

      const res = await request(app)
        .get('/api/v1/riders/stats')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toHaveProperty('rating', 4.5);
      expect(res.body.data).toHaveProperty('totalDeliveries', 50);
      expect(res.body.data).toHaveProperty('todayDeliveries', 5);
      expect(res.body.data).toHaveProperty('todayEarnings', 25000);
      expect(res.body.data).toHaveProperty('status', 'available');
    });
  });

  // ─── Admin Operations (via /api/v1/riders/admin/) ──────────────────
  // Admin routes are now defined before riderAuth middleware, using adminAuth.

  describe('Admin rider management', () => {
    it('should allow admin to get all riders', async () => {
      const token = generateAdminToken();
      User.findById.mockResolvedValue({
        _id: '507f1f77bcf86cd799439033',
        role: 'admin',
        phone: '+8801700000000',
        name: 'Admin',
      });
      Rider.find.mockReturnValue(chainableQuery([mockRider]));
      Rider.countDocuments.mockResolvedValue(1);

      const res = await request(app)
        .get('/api/v1/riders/admin/all')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toHaveProperty('riders');
      expect(res.body.data).toHaveProperty('pagination');
    });

    it('should allow admin to approve a rider', async () => {
      const token = generateAdminToken();
      User.findById.mockResolvedValue({
        _id: '507f1f77bcf86cd799439033',
        role: 'admin',
        phone: '+8801700000000',
        name: 'Admin',
      });
      Rider.findByIdAndUpdate.mockResolvedValue({ ...mockRider, isApproved: true });

      const res = await request(app)
        .patch('/api/v1/riders/admin/507f1f77bcf86cd799439044/approve')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.message).toBe('Rider approved successfully');
    });

    it('should allow admin to suspend a rider', async () => {
      const token = generateAdminToken();
      User.findById.mockResolvedValue({
        _id: '507f1f77bcf86cd799439033',
        role: 'admin',
        phone: '+8801700000000',
        name: 'Admin',
      });
      Rider.findByIdAndUpdate.mockResolvedValue({
        ...mockRider,
        isApproved: false,
        status: 'offline',
        suspensionReason: 'Policy violation',
      });

      const res = await request(app)
        .patch('/api/v1/riders/admin/507f1f77bcf86cd799439044/suspend')
        .set('Authorization', `Bearer ${token}`)
        .send({ reason: 'Policy violation' });

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.message).toBe('Rider suspended successfully');
    });

    it('should reject non-admin users from admin routes', async () => {
      const token = generateRiderToken();
      // riderAuth token has riderId but no userId → auth middleware can't find a User
      User.findById.mockResolvedValue(null);

      const res = await request(app)
        .get('/api/v1/riders/admin/all')
        .set('Authorization', `Bearer ${token}`);

      // adminAuth → auth finds no user → 401
      expect(res.statusCode).toBeGreaterThanOrEqual(401);
      expect(res.body.success).toBe(false);
    });
  });
});
