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
    userId: '507f1f77bcf86cd799439011',
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
  Model.findByIdAndUpdate = jest.fn();
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
  Model.findById = jest.fn();
  Model.schema = { path: jest.fn() };
  return Model;
});

// Mock Review model
jest.mock('../src/models/Review', () => {
  const Model = jest.fn();
  Model.find = jest.fn();
  Model.findById = jest.fn();
  Model.findOne = jest.fn();
  Model.create = jest.fn();
  Model.countDocuments = jest.fn();
  Model.aggregate = jest.fn();
  Model.schema = { path: jest.fn() };
  return Model;
});

// Mock Rider model
jest.mock('../src/models/Rider', () => {
  const Model = jest.fn();
  Model.find = jest.fn();
  Model.findById = jest.fn();
  Model.findByIdAndUpdate = jest.fn();
  Model.schema = { path: jest.fn() };
  return Model;
});

const app = require('../src/app');
const User = require('../src/models/User');
const Review = require('../src/models/Review');
const Order = require('../src/models/Order');
const Restaurant = require('../src/models/Restaurant');
const Rider = require('../src/models/Rider');

// Set test environment variables
process.env.JWT_SECRET = 'test-jwt-secret-key-minimum-32-characters-long';
process.env.REFRESH_TOKEN_SECRET = 'test-refresh-token-secret-key-minimum-32-chars';
process.env.JWT_EXPIRES_IN = '15m';
process.env.REFRESH_TOKEN_EXPIRES_IN = '7d';

// Helper: generate a regular user JWT token
const generateUserToken = (userId = '507f1f77bcf86cd799439011') => {
  return jwt.sign({ userId }, process.env.JWT_SECRET, { expiresIn: '15m' });
};

// Helper: generate a restaurant owner JWT token (goes through protect middleware)
const generateRestaurantOwnerToken = (userId = '507f1f77bcf86cd799439033') => {
  return jwt.sign({ userId }, process.env.JWT_SECRET, { expiresIn: '15m' });
};

// Helper: generate an admin JWT token
const generateAdminToken = (userId = '507f1f77bcf86cd799439044') => {
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

// Mock review data
const mockReview = {
  _id: '507f1f77bcf86cd799439066',
  order: '507f1f77bcf86cd799439055',
  user: '507f1f77bcf86cd799439011',
  restaurant: '507f1f77bcf86cd799439022',
  rider: '507f1f77bcf86cd799439077',
  foodRating: 4,
  deliveryRating: 5,
  review: 'Great food and fast delivery!',
  reviewBn: 'দারুণ খাবার!',
  images: [],
  response: null,
  isPublished: true,
};

// Mock order data (uses field names the controller accesses)
const mockOrder = {
  _id: '507f1f77bcf86cd799439055',
  userId: '507f1f77bcf86cd799439011',
  restaurantId: '507f1f77bcf86cd799439022',
  riderId: '507f1f77bcf86cd799439077',
  status: 'delivered',
  orderNumber: 'KE-0001',
};

// Mock user objects for auth middleware
const mockCustomerUser = {
  _id: '507f1f77bcf86cd799439011',
  userId: '507f1f77bcf86cd799439011',
  role: 'customer',
  name: 'Test User',
  phone: '+8801712345678',
};

const mockRestaurantOwnerUser = {
  _id: '507f1f77bcf86cd799439033',
  userId: '507f1f77bcf86cd799439033',
  role: 'restaurant',
  name: 'Restaurant Owner',
  phone: '+8801812345678',
};

const mockAdminUser = {
  _id: '507f1f77bcf86cd799439044',
  userId: '507f1f77bcf86cd799439044',
  role: 'admin',
  name: 'Admin User',
  phone: '+8801700000000',
};

describe('Review Endpoints', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  // ─── Public Routes ────────────────────────────────────────────────

  describe('GET /api/v1/reviews/restaurant/:restaurantId', () => {
    it('should return restaurant reviews with pagination', async () => {
      Review.find.mockReturnValue(chainableQuery([mockReview]));
      Review.countDocuments.mockResolvedValue(1);
      Review.aggregate.mockResolvedValue([
        { _id: 5, count: 0 },
        { _id: 4, count: 1 },
      ]);

      const res = await request(app)
        .get(`/api/v1/reviews/restaurant/${mockReview.restaurant}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data.reviews).toHaveLength(1);
      expect(res.body.data.pagination).toHaveProperty('total', 1);
      expect(res.body.data.pagination).toHaveProperty('page', 1);
      expect(res.body.data).toHaveProperty('distribution');
    });

    it('should return empty reviews for restaurant with no reviews', async () => {
      Review.find.mockReturnValue(chainableQuery([]));
      Review.countDocuments.mockResolvedValue(0);
      Review.aggregate.mockResolvedValue([]);

      const res = await request(app)
        .get('/api/v1/reviews/restaurant/507f1f77bcf86cd799439099');

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data.reviews).toHaveLength(0);
      expect(res.body.data.pagination.total).toBe(0);
    });
  });

  // ─── Authentication Checks ────────────────────────────────────────

  describe('Authentication requirements', () => {
    it('should reject protected routes without token', async () => {
      const res = await request(app)
        .post('/api/v1/reviews')
        .send({ orderId: '507f1f77bcf86cd799439055', foodRating: 4 });

      expect(res.statusCode).toBe(401);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('No authentication token provided');
    });

    it('should reject protected routes with invalid token', async () => {
      const res = await request(app)
        .post('/api/v1/reviews')
        .set('Authorization', 'Bearer invalid-token')
        .send({ orderId: '507f1f77bcf86cd799439055', foodRating: 4 });

      expect(res.statusCode).toBe(401);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Invalid token');
    });
  });

  // ─── User Routes ──────────────────────────────────────────────────

  describe('POST /api/v1/reviews', () => {
    it('should create a review for a delivered order', async () => {
      const token = generateUserToken();
      User.findById.mockResolvedValue(mockCustomerUser);
      Order.findById.mockResolvedValue(mockOrder);
      Review.findOne.mockResolvedValue(null);
      Review.create.mockResolvedValue(mockReview);
      Review.find.mockReturnValue(chainableQuery([mockReview]));
      Restaurant.findByIdAndUpdate.mockResolvedValue(true);
      Rider.findByIdAndUpdate.mockResolvedValue(true);

      const res = await request(app)
        .post('/api/v1/reviews')
        .set('Authorization', `Bearer ${token}`)
        .send({
          orderId: '507f1f77bcf86cd799439055',
          foodRating: 4,
          deliveryRating: 5,
          review: 'Great food!',
        });

      expect(res.statusCode).toBe(201);
      expect(res.body.success).toBe(true);
      expect(res.body.message).toBe('Review submitted successfully');
      expect(res.body.data).toHaveProperty('foodRating', 4);
    });

    it('should return 404 when order not found', async () => {
      const token = generateUserToken();
      User.findById.mockResolvedValue(mockCustomerUser);
      Order.findById.mockResolvedValue(null);

      const res = await request(app)
        .post('/api/v1/reviews')
        .set('Authorization', `Bearer ${token}`)
        .send({
          orderId: '507f1f77bcf86cd799439099',
          foodRating: 4,
        });

      expect(res.statusCode).toBe(404);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Order not found');
    });

    it('should return 400 when order is not delivered', async () => {
      const token = generateUserToken();
      User.findById.mockResolvedValue(mockCustomerUser);
      Order.findById.mockResolvedValue({
        ...mockOrder,
        status: 'preparing',
      });

      const res = await request(app)
        .post('/api/v1/reviews')
        .set('Authorization', `Bearer ${token}`)
        .send({
          orderId: '507f1f77bcf86cd799439055',
          foodRating: 4,
        });

      expect(res.statusCode).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Can only review delivered orders');
    });

    it('should return 400 when review already exists for order', async () => {
      const token = generateUserToken();
      User.findById.mockResolvedValue(mockCustomerUser);
      Order.findById.mockResolvedValue(mockOrder);
      Review.findOne.mockResolvedValue(mockReview);

      const res = await request(app)
        .post('/api/v1/reviews')
        .set('Authorization', `Bearer ${token}`)
        .send({
          orderId: '507f1f77bcf86cd799439055',
          foodRating: 4,
        });

      expect(res.statusCode).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Review already exists for this order');
    });

    it('should return 400 when validation fails (missing orderId)', async () => {
      const token = generateUserToken();
      User.findById.mockResolvedValue(mockCustomerUser);

      const res = await request(app)
        .post('/api/v1/reviews')
        .set('Authorization', `Bearer ${token}`)
        .send({ foodRating: 4 });

      expect(res.statusCode).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Validation failed');
    });
  });

  describe('GET /api/v1/reviews/my-reviews', () => {
    it("should return the authenticated user's reviews", async () => {
      const token = generateUserToken();
      User.findById.mockResolvedValue(mockCustomerUser);
      Review.find.mockReturnValue(chainableQuery([mockReview]));
      Review.countDocuments.mockResolvedValue(1);

      const res = await request(app)
        .get('/api/v1/reviews/my-reviews')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data.reviews).toHaveLength(1);
      expect(res.body.data.pagination).toHaveProperty('total', 1);
    });
  });

  describe('PUT /api/v1/reviews/:reviewId', () => {
    it('should update a review', async () => {
      const token = generateUserToken();
      User.findById.mockResolvedValue(mockCustomerUser);
      const saveMock = jest.fn().mockResolvedValue(true);
      Review.findById.mockResolvedValue({
        ...mockReview,
        user: { toString: () => '507f1f77bcf86cd799439011' },
        save: saveMock,
      });
      Review.find.mockReturnValue(chainableQuery([{ ...mockReview, foodRating: 5 }]));
      Restaurant.findByIdAndUpdate.mockResolvedValue(true);

      const res = await request(app)
        .put(`/api/v1/reviews/${mockReview._id}`)
        .set('Authorization', `Bearer ${token}`)
        .send({ foodRating: 5, review: 'Updated review' });

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.message).toBe('Review updated successfully');
      expect(saveMock).toHaveBeenCalled();
    });

    it('should return 404 when review not found for update', async () => {
      const token = generateUserToken();
      User.findById.mockResolvedValue(mockCustomerUser);
      Review.findById.mockResolvedValue(null);

      const res = await request(app)
        .put('/api/v1/reviews/507f1f77bcf86cd799439099')
        .set('Authorization', `Bearer ${token}`)
        .send({ foodRating: 5 });

      expect(res.statusCode).toBe(404);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Review not found');
    });

    it('should return 403 when user is not the review owner', async () => {
      const token = generateUserToken();
      User.findById.mockResolvedValue(mockCustomerUser);
      Review.findById.mockResolvedValue({
        ...mockReview,
        user: { toString: () => '507f1f77bcf86cd799439099' },
      });

      const res = await request(app)
        .put(`/api/v1/reviews/${mockReview._id}`)
        .set('Authorization', `Bearer ${token}`)
        .send({ foodRating: 5 });

      expect(res.statusCode).toBe(403);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Not authorized to update this review');
    });
  });

  describe('DELETE /api/v1/reviews/:reviewId', () => {
    it('should delete a review and recalculate ratings', async () => {
      const token = generateUserToken();
      User.findById.mockResolvedValue(mockCustomerUser);
      const deleteOneMock = jest.fn().mockResolvedValue(true);
      Review.findById.mockResolvedValue({
        ...mockReview,
        user: { toString: () => '507f1f77bcf86cd799439011' },
        deleteOne: deleteOneMock,
      });
      Review.find.mockReturnValue(chainableQuery([]));
      Restaurant.findByIdAndUpdate.mockResolvedValue(true);
      Rider.findByIdAndUpdate.mockResolvedValue(true);

      const res = await request(app)
        .delete(`/api/v1/reviews/${mockReview._id}`)
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.message).toBe('Review deleted successfully');
      expect(deleteOneMock).toHaveBeenCalled();
    });
  });

  // ─── Restaurant Owner Routes ──────────────────────────────────────

  describe('POST /api/v1/reviews/:reviewId/respond', () => {
    it('should allow restaurant owner to respond to a review', async () => {
      const token = generateRestaurantOwnerToken();
      User.findById.mockResolvedValue(mockRestaurantOwnerUser);
      const saveMock = jest.fn().mockResolvedValue(true);
      Review.findById.mockResolvedValue({
        ...mockReview,
        save: saveMock,
      });
      Restaurant.findById.mockResolvedValue({
        _id: '507f1f77bcf86cd799439022',
        ownerId: { toString: () => '507f1f77bcf86cd799439033' },
        name: 'Test Restaurant',
      });

      const res = await request(app)
        .post(`/api/v1/reviews/${mockReview._id}/respond`)
        .set('Authorization', `Bearer ${token}`)
        .send({ response: 'Thank you for your feedback!' });

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.message).toBe('Response added successfully');
      expect(saveMock).toHaveBeenCalled();
    });

    it('should reject response from non-restaurant role user', async () => {
      const token = generateUserToken();
      User.findById.mockResolvedValue(mockCustomerUser);

      const res = await request(app)
        .post(`/api/v1/reviews/${mockReview._id}/respond`)
        .set('Authorization', `Bearer ${token}`)
        .send({ response: 'Thanks!' });

      expect(res.statusCode).toBe(403);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Access denied. Required role(s): restaurant');
    });
  });

  // ─── Admin Routes ─────────────────────────────────────────────────

  describe('GET /api/v1/reviews', () => {
    it('should return all reviews for admin', async () => {
      const token = generateAdminToken();
      User.findById.mockResolvedValue(mockAdminUser);
      Review.find.mockReturnValue(chainableQuery([mockReview]));
      Review.countDocuments.mockResolvedValue(1);

      const res = await request(app)
        .get('/api/v1/reviews')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data.reviews).toHaveLength(1);
      expect(res.body.data.pagination).toHaveProperty('total', 1);
    });

    it('should reject non-admin users from getting all reviews', async () => {
      const token = generateUserToken();
      User.findById.mockResolvedValue(mockCustomerUser);

      const res = await request(app)
        .get('/api/v1/reviews')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(403);
      expect(res.body.success).toBe(false);
    });
  });

  describe('PATCH /api/v1/reviews/:reviewId/toggle-visibility', () => {
    it('should toggle review visibility for admin', async () => {
      const token = generateAdminToken();
      User.findById.mockResolvedValue(mockAdminUser);
      const saveMock = jest.fn().mockResolvedValue(true);
      Review.findById.mockResolvedValue({
        ...mockReview,
        isPublished: true,
        save: saveMock,
      });

      const res = await request(app)
        .patch(`/api/v1/reviews/${mockReview._id}/toggle-visibility`)
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.message).toMatch(/Review (published|hidden) successfully/);
      expect(saveMock).toHaveBeenCalled();
    });

    it('should return 404 when review not found for visibility toggle', async () => {
      const token = generateAdminToken();
      User.findById.mockResolvedValue(mockAdminUser);
      Review.findById.mockResolvedValue(null);

      const res = await request(app)
        .patch('/api/v1/reviews/507f1f77bcf86cd799439099/toggle-visibility')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(404);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Review not found');
    });

    it('should reject non-admin users from toggling visibility', async () => {
      const token = generateUserToken();
      User.findById.mockResolvedValue(mockCustomerUser);

      const res = await request(app)
        .patch(`/api/v1/reviews/${mockReview._id}/toggle-visibility`)
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(403);
      expect(res.body.success).toBe(false);
    });
  });
});
