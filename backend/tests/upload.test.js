const request = require('supertest');
const jwt = require('jsonwebtoken');
const path = require('path');
const fs = require('fs');

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

// Mock multer upload middleware
jest.mock('../src/middleware/upload', () => ({
  single: jest.fn(() => (req, res, next) => {
    // Simulate no file uploaded by default; tests override req.file as needed
    next();
  }),
  multiple: jest.fn(() => (req, res, next) => {
    next();
  }),
  fields: jest.fn(() => (req, res, next) => {
    next();
  }),
}));

// Mock fs module for file operations in uploadController
jest.mock('fs', () => {
  const actual = jest.requireActual('fs');
  return {
    ...actual,
    existsSync: jest.fn(),
    unlinkSync: jest.fn(),
    statSync: jest.fn(),
    mkdirSync: jest.fn(),
  };
});

// Mock remaining models loaded transitively by the app
jest.mock('../src/models/Restaurant', () => {
  const Model = jest.fn();
  Model.find = jest.fn();
  Model.findById = jest.fn();
  Model.findOne = jest.fn();
  Model.countDocuments = jest.fn();
  Model.schema = { path: jest.fn() };
  return Model;
});

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

jest.mock('../src/models/Zone', () => {
  const Model = jest.fn();
  Model.find = jest.fn();
  Model.findById = jest.fn();
  Model.schema = { path: jest.fn() };
  return Model;
});

const app = require('../src/app');
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

// Helper: mock regular user for auth middleware
const mockRegularUser = () => {
  User.findById.mockResolvedValue({
    _id: '507f1f77bcf86cd799439011',
    role: 'customer',
    phone: '+8801712345678',
    name: 'User',
  });
};

describe('Upload Endpoints', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  // ─── Authentication Requirements ───────────────────────────────────

  describe('Authentication requirements', () => {
    it('should reject upload requests without token', async () => {
      const res = await request(app).post('/api/v1/upload/restaurant');

      expect(res.statusCode).toBe(401);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('No authentication token provided');
    });

    it('should reject delete requests without token', async () => {
      const res = await request(app).delete('/api/v1/upload/test-file.jpg');

      expect(res.statusCode).toBe(401);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('No authentication token provided');
    });

    it('should reject file info requests without token', async () => {
      const res = await request(app).get('/api/v1/upload/test-file.jpg/info');

      expect(res.statusCode).toBe(401);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('No authentication token provided');
    });
  });

  // ─── Upload: No File ───────────────────────────────────────────────

  describe('POST /api/v1/upload/restaurant', () => {
    it('should return 400 when no file is uploaded', async () => {
      const token = generateUserToken();
      mockRegularUser();

      const res = await request(app)
        .post('/api/v1/upload/restaurant')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('No file uploaded');
    });
  });

  describe('POST /api/v1/upload/restaurant/multiple', () => {
    it('should return 400 when no files are uploaded', async () => {
      const token = generateUserToken();
      mockRegularUser();

      const res = await request(app)
        .post('/api/v1/upload/restaurant/multiple')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('No files uploaded');
    });
  });

  describe('POST /api/v1/upload/menu', () => {
    it('should return 400 when no menu image is uploaded', async () => {
      const token = generateUserToken();
      mockRegularUser();

      const res = await request(app)
        .post('/api/v1/upload/menu')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(400);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('No file uploaded');
    });
  });

  // ─── Delete File ───────────────────────────────────────────────────

  describe('DELETE /api/v1/upload/:filename', () => {
    it('should delete an existing file', async () => {
      const token = generateUserToken();
      mockRegularUser();

      fs.existsSync.mockReturnValue(true);
      fs.unlinkSync.mockReturnValue(undefined);

      const res = await request(app)
        .delete('/api/v1/upload/image-12345.jpg')
        .query({ type: 'restaurants' })
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.message).toBe('File deleted successfully');
      expect(fs.unlinkSync).toHaveBeenCalled();
    });

    it('should return 404 when file does not exist', async () => {
      const token = generateUserToken();
      mockRegularUser();

      fs.existsSync.mockReturnValue(false);

      const res = await request(app)
        .delete('/api/v1/upload/nonexistent.jpg')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(404);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('File not found');
    });
  });

  // ─── Get File Info ─────────────────────────────────────────────────

  describe('GET /api/v1/upload/:filename/info', () => {
    it('should return file info for existing file', async () => {
      const token = generateUserToken();
      mockRegularUser();

      const mockStats = {
        size: 102400,
        birthtime: new Date('2024-01-15T10:00:00.000Z'),
        mtime: new Date('2024-01-15T12:00:00.000Z'),
      };

      fs.existsSync.mockReturnValue(true);
      fs.statSync.mockReturnValue(mockStats);

      const res = await request(app)
        .get('/api/v1/upload/image-12345.jpg/info')
        .query({ type: 'restaurants' })
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toHaveProperty('filename', 'image-12345.jpg');
      expect(res.body.data).toHaveProperty('size', 102400);
      expect(res.body.data).toHaveProperty('createdAt');
      expect(res.body.data).toHaveProperty('modifiedAt');
    });

    it('should return 404 when file does not exist', async () => {
      const token = generateUserToken();
      mockRegularUser();

      fs.existsSync.mockReturnValue(false);

      const res = await request(app)
        .get('/api/v1/upload/nonexistent.jpg/info')
        .set('Authorization', `Bearer ${token}`);

      expect(res.statusCode).toBe(404);
      expect(res.body.success).toBe(false);
      expect(res.body.message).toBe('File not found');
    });
  });
});
