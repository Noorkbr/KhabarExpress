const jwt = require('jsonwebtoken');
const User = require('../models/User');
const otpService = require('../services/otpService');

// Generate JWT tokens
const generateTokens = (userId) => {
  const accessToken = jwt.sign(
    { userId },
    process.env.JWT_SECRET,
    { expiresIn: process.env.JWT_EXPIRES_IN || '15m' }
  );

  const refreshToken = jwt.sign(
    { userId },
    process.env.REFRESH_TOKEN_SECRET,
    { expiresIn: process.env.REFRESH_TOKEN_EXPIRES_IN || '7d' }
  );

  return { accessToken, refreshToken };
};

// Send OTP to Bangladesh phone number
exports.sendOtp = async (req, res, next) => {
  try {
    const { phone } = req.body;

    // Validate Bangladesh phone format
    if (!phone || !/^\+880\d{10}$/.test(phone)) {
      return res.status(400).json({
        success: false,
        message: 'Invalid Bangladesh phone number. Format: +880XXXXXXXXXX',
      });
    }

    // Generate and send OTP
    const otpSent = await otpService.sendOTP(phone);

    if (!otpSent) {
      return res.status(500).json({
        success: false,
        message: 'Failed to send OTP. Please try again.',
      });
    }

    res.status(200).json({
      success: true,
      message: 'OTP sent successfully',
      data: {
        phone,
        expiresIn: process.env.OTP_EXPIRY_MINUTES || 10,
      },
    });
  } catch (error) {
    next(error);
  }
};

// Verify OTP and login/register
exports.verifyOtp = async (req, res, next) => {
  try {
    const { phone, otp } = req.body;

    if (!phone || !otp) {
      return res.status(400).json({
        success: false,
        message: 'Phone number and OTP are required',
      });
    }

    // Verify OTP
    const isValid = await otpService.verifyOTP(phone, otp);

    if (!isValid) {
      return res.status(400).json({
        success: false,
        message: 'Invalid or expired OTP',
      });
    }

    // Find or create user
    let user = await User.findOne({ phone });

    if (!user) {
      // Create new user
      user = await User.create({
        phone,
        name: 'User', // Default name, can be updated later
        isVerified: true,
      });
    } else {
      // Mark existing user as verified
      user.isVerified = true;
      await user.save();
    }

    // Generate tokens
    const { accessToken, refreshToken } = generateTokens(user._id);

    res.status(200).json({
      success: true,
      message: 'Login successful',
      data: {
        user: {
          id: user._id,
          phone: user.phone,
          name: user.name,
          email: user.email,
          profileImage: user.profileImage,
          preferredLanguage: user.preferredLanguage,
        },
        accessToken,
        refreshToken,
      },
    });
  } catch (error) {
    next(error);
  }
};

// Refresh access token
exports.refreshToken = async (req, res, next) => {
  try {
    const { refreshToken } = req.body;

    if (!refreshToken) {
      return res.status(400).json({
        success: false,
        message: 'Refresh token is required',
      });
    }

    // Verify refresh token
    const decoded = jwt.verify(refreshToken, process.env.REFRESH_TOKEN_SECRET);

    // Check if user exists
    const user = await User.findById(decoded.userId);

    if (!user) {
      return res.status(401).json({
        success: false,
        message: 'User not found',
      });
    }

    // Generate new access token
    const accessToken = jwt.sign(
      { userId: user._id },
      process.env.JWT_SECRET,
      { expiresIn: process.env.JWT_EXPIRES_IN || '15m' }
    );

    res.status(200).json({
      success: true,
      data: {
        accessToken,
      },
    });
  } catch (error) {
    if (error.name === 'JsonWebTokenError' || error.name === 'TokenExpiredError') {
      return res.status(401).json({
        success: false,
        message: 'Invalid or expired refresh token',
      });
    }
    next(error);
  }
};

// Logout (optional - mainly for client-side token cleanup)
exports.logout = async (req, res, next) => {
  try {
    // In a stateless JWT setup, logout is handled client-side
    // Optionally, implement token blacklisting with Redis
    res.status(200).json({
      success: true,
      message: 'Logged out successfully',
    });
  } catch (error) {
    next(error);
  }
};
