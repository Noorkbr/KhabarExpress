const express = require('express');
const router = express.Router();
const adminController = require('../controllers/adminController');
const { protect, authorize } = require('../middleware/auth');

// All admin routes require admin authorization
router.use(protect);
router.use(authorize('admin'));

// Dashboard & Analytics
router.get('/dashboard', adminController.getDashboardStats);
router.get('/activities', adminController.getRecentActivities);
router.get('/analytics', adminController.getAnalytics);
router.get('/revenue', adminController.getRevenueAnalytics);
router.get('/user-growth', adminController.getUserGrowthAnalytics);
router.get('/export', adminController.exportData);
router.get('/profit', adminController.getProfitAnalytics);
router.get('/financial-report', adminController.exportFinancialReport);
router.get('/verification-stats', adminController.getVerificationStats);

// Users Management
router.get('/users', adminController.listUsers);
router.get('/users/:id', adminController.getUserById);
router.patch('/users/:id', adminController.updateUser);

// Orders Management
router.get('/orders', adminController.listOrders);
router.get('/orders/:id', adminController.getOrderById);
router.patch('/orders/:id/status', adminController.updateOrderStatus);

// Restaurants Management
router.get('/restaurants', adminController.listRestaurants);
router.patch('/restaurants/:id/status', adminController.updateRestaurantStatus);

// Riders Management
router.get('/riders', adminController.listRiders);

// Payments Management
router.get('/payments', adminController.listPayments);

// Settings Management
router.get('/settings', adminController.getSettings);
router.patch('/settings', adminController.updateSettings);

module.exports = router;
