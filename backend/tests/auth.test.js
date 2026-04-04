const request = require('supertest');
const jwt = require('jsonwebtoken');

// Mock rate limiters to prevent throttling during tests
jest.mock('../src/middleware/rateLimiter', () => ({
  otpLimiter: (req, res, next) => next(),
  authLimiter: (req, res, next) => next(),
  apiLimiter: (req, res, next) => next(),
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

const app = require('../src/app');
const otpService = require('../src/services/otpService');
const User = require('../src/models/User');

// Set test environment variables
process.env.JWT_SECRET = 'test-jwt-secret-key-minimum-32-characters-long';
process.env.REFRESH_TOKEN_SECRET = 'test-refresh-token-secret-key-minimum-32-chars';
process.env.JWT_EXPIRES_IN = '15m';
process.env.REFRESH_TOKEN_EXPIRES_IN = '7d';

describe('Auth Endpoints', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('POST /api/v1/auth/send-otp', () => {
    it('should send OTP for valid Bangladesh phone number', async () => {
      otpService.sendOTP.mockResolvedValue(true);

      const res = await request(app)
        .post('/api/v1/auth/send-otp')
        .send({ phone: '+8801712345678' });

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.message).toBe('OTP sent successfully');
      expect(res.body.data.phone).toBe('+8801712345678');
      expect(otpService.sendOTP).toHaveBeenCalledWith('+8801712345678');
    });

    it('should reject invalid phone number format', async () => {
      const res = await request(app)
        .post('/api/v1/auth/send-otp')
        .send({ phone: '01712345678' });

      expect(res.statusCode).toBe(400);
      expect(res.body.success).toBe(false);
    });

    it('should reject missing phone number', async () => {
      const res = await request(app)
        .post('/api/v1/auth/send-otp')
        .send({});

      expect(res.statusCode).toBe(400);
      expect(res.body.success).toBe(false);
    });

    it('should return 500 when OTP service fails', async () => {
      otpService.sendOTP.mockResolvedValue(false);

      const res = await request(app)
        .post('/api/v1/auth/send-otp')
        .send({ phone: '+8801712345678' });

      expect(res.statusCode).toBe(500);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Failed to send OTP. Please try again.');
    });
  });

  describe('POST /api/v1/auth/verify-otp', () => {
    const mockUser = {
      _id: '507f1f77bcf86cd799439011',
      phone: '+8801712345678',
      name: 'User',
      email: null,
      profileImage: null,
      preferredLanguage: 'en',
      isVerified: true,
      save: jest.fn().mockResolvedValue(true),
    };

    it('should verify OTP and login existing user', async () => {
      otpService.verifyOTP.mockResolvedValue(true);
      User.findOne.mockResolvedValue(mockUser);

      const res = await request(app)
        .post('/api/v1/auth/verify-otp')
        .send({ phone: '+8801712345678', otp: '123456' });

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.message).toBe('Login successful');
      expect(res.body.data).toHaveProperty('accessToken');
      expect(res.body.data).toHaveProperty('refreshToken');
      expect(res.body.data.user.phone).toBe('+8801712345678');
    });

    it('should create new user if not found', async () => {
      otpService.verifyOTP.mockResolvedValue(true);
      User.findOne.mockResolvedValue(null);
      User.create.mockResolvedValue(mockUser);

      const res = await request(app)
        .post('/api/v1/auth/verify-otp')
        .send({ phone: '+8801712345678', otp: '123456' });

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(User.create).toHaveBeenCalledWith(
        expect.objectContaining({
          phone: '+8801712345678',
          isVerified: true,
        })
      );
    });

    it('should reject invalid OTP', async () => {
      otpService.verifyOTP.mockResolvedValue(false);

      const res = await request(app)
        .post('/api/v1/auth/verify-otp')
        .send({ phone: '+8801712345678', otp: '000000' });

      expect(res.statusCode).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('Invalid or expired OTP');
    });

    it('should reject missing phone or OTP', async () => {
      const res = await request(app)
        .post('/api/v1/auth/verify-otp')
        .send({ phone: '+8801712345678' });

      expect(res.statusCode).toBe(400);
      expect(res.body.success).toBe(false);
    });
  });

  describe('POST /api/v1/auth/refresh-token', () => {
    it('should return new access token for valid refresh token', async () => {
      const userId = '507f1f77bcf86cd799439011';
      const refreshToken = jwt.sign(
        { userId },
        process.env.REFRESH_TOKEN_SECRET,
        { expiresIn: '7d' }
      );

      User.findById.mockResolvedValue({
        _id: userId,
        phone: '+8801712345678',
        name: 'User',
      });

      const res = await request(app)
        .post('/api/v1/auth/refresh-token')
        .send({ refreshToken });

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toHaveProperty('accessToken');
    });

    it('should reject missing refresh token', async () => {
      const res = await request(app)
        .post('/api/v1/auth/refresh-token')
        .send({});

      expect(res.statusCode).toBe(400);
      expect(res.body.success).toBe(false);
    });

    it('should reject invalid refresh token', async () => {
      const res = await request(app)
        .post('/api/v1/auth/refresh-token')
        .send({ refreshToken: 'invalid-token' });

      expect(res.statusCode).toBe(401);
      expect(res.body.success).toBe(false);
    });

    it('should reject when user not found', async () => {
      const refreshToken = jwt.sign(
        { userId: '507f1f77bcf86cd799439099' },
        process.env.REFRESH_TOKEN_SECRET,
        { expiresIn: '7d' }
      );
      User.findById.mockResolvedValue(null);

      const res = await request(app)
        .post('/api/v1/auth/refresh-token')
        .send({ refreshToken });

      expect(res.statusCode).toBe(401);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('User not found');
    });
  });

  describe('POST /api/v1/auth/logout', () => {
    it('should return success on logout', async () => {
      const res = await request(app)
        .post('/api/v1/auth/logout');

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.message).toBe('Logged out successfully');
    });
  });
});
