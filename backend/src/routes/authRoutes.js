const express = require('express');
const router = express.Router();
const { body } = require('express-validator');
const authController = require('../controllers/authController');
const validate = require('../middleware/validator');
const { otpLimiter, authLimiter } = require('../middleware/rateLimiter');

// Send OTP
router.post('/send-otp',
  otpLimiter,
  [
    body('phone')
      .matches(/^\+880\d{10}$/)
      .withMessage('Invalid Bangladesh phone number. Format: +880XXXXXXXXXX'),
  ],
  validate,
  authController.sendOtp
);

// Verify OTP and login/register
router.post('/verify-otp',
  authLimiter,
  [
    body('phone').notEmpty().withMessage('Phone number is required'),
    body('otp')
      .isLength({ min: 6, max: 6 })
      .withMessage('OTP must be 6 digits'),
  ],
  validate,
  authController.verifyOtp
);

// Refresh access token
router.post('/refresh-token',
  [
    body('refreshToken').notEmpty().withMessage('Refresh token is required'),
  ],
  validate,
  authController.refreshToken
);

// Logout
router.post('/logout', authController.logout);

module.exports = router;
