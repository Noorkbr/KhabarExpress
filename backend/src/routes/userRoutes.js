const express = require('express');
const router = express.Router();
const { body } = require('express-validator');
const userController = require('../controllers/userController');
const { auth } = require('../middleware/auth');
const validate = require('../middleware/validator');

// All user routes require authentication
router.use(auth);

// Get user profile
router.get('/profile', userController.getProfile);

// Update user profile
router.patch('/profile',
  [
    body('name').optional().isLength({ min: 2 }).withMessage('Name must be at least 2 characters'),
    body('email').optional().isEmail().withMessage('Invalid email address'),
    body('preferredLanguage').optional().isIn(['en', 'bn']).withMessage('Language must be en or bn'),
  ],
  validate,
  userController.updateProfile
);

// Address management
router.post('/addresses',
  [
    body('coordinates').isArray({ min: 2, max: 2 }).withMessage('Coordinates must be [longitude, latitude]'),
    body('area').notEmpty().withMessage('Area is required'),
  ],
  validate,
  userController.addAddress
);

router.patch('/addresses/:addressId', userController.updateAddress);
router.delete('/addresses/:addressId', userController.deleteAddress);

// Favorites
router.post('/favorites/:restaurantId', userController.toggleFavorite);
router.get('/favorites', userController.getFavorites);

module.exports = router;
