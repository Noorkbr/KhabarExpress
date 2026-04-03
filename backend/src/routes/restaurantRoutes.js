const express = require('express');
const router = express.Router();
const restaurantController = require('../controllers/restaurantController');
const { auth, adminAuth, restaurantAuth } = require('../middleware/auth');

// Public routes
router.get('/', restaurantController.getRestaurants);
router.get('/featured', restaurantController.getFeaturedRestaurants);
router.get('/search', restaurantController.searchAll);
router.get('/:id', restaurantController.getRestaurantById);

// Restaurant self-service routes
router.post('/register', restaurantController.registerRestaurant);
router.put('/profile', restaurantAuth, restaurantController.updateProfile);
router.put('/location', restaurantAuth, restaurantController.updateLocation);
router.put('/business-hours', restaurantAuth, restaurantController.updateBusinessHours);
router.patch('/status', restaurantAuth, restaurantController.toggleOpenStatus);
router.get('/my/analytics', restaurantAuth, restaurantController.getAnalytics);

// Admin routes
router.get('/admin/all', adminAuth, restaurantController.getAllRestaurants);
router.get('/admin/pending', adminAuth, restaurantController.getPendingRestaurants);
router.patch('/admin/:id/approve', adminAuth, restaurantController.approveRestaurant);
router.patch('/admin/:id/reject', adminAuth, restaurantController.rejectRestaurant);
router.patch('/admin/:id/suspend', adminAuth, restaurantController.suspendRestaurant);

module.exports = router;
