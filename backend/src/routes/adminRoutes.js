const express = require('express');
const router = express.Router();
const adminController = require('../controllers/adminController');
const { protect, authorize } = require('../middleware/auth');

// All admin routes require admin authorization
router.use(protect);
router.use(authorize('admin'));

router.get('/dashboard', adminController.getDashboardStats);
router.get('/activities', adminController.getRecentActivities);
router.get('/analytics', adminController.getAnalytics);
router.get('/revenue', adminController.getRevenueAnalytics);
router.get('/user-growth', adminController.getUserGrowthAnalytics);
router.get('/export', adminController.exportData);

module.exports = router;
