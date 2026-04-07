const express = require('express');
const router = express.Router();
const riderController = require('../controllers/riderController');
const { riderAuth, adminAuth } = require('../middleware/auth');
const { body } = require('express-validator');
const validate = require('../middleware/validator');

// Public routes
router.post(
  '/register',
  [
    body('name').notEmpty().withMessage('Name is required'),
    body('phone').matches(/^\+880\d{10}$/).withMessage('Invalid Bangladesh phone number'),
    body('vehicleType').isIn(['motorcycle', 'bicycle', 'car']).withMessage('Invalid vehicle type'),
    validate,
  ],
  riderController.registerRider
);

// Admin routes (must be before riderAuth middleware)
router.get('/admin/all', adminAuth, riderController.getAllRiders);
router.patch('/admin/:riderId/approve', adminAuth, riderController.approveRider);
router.patch('/admin/:riderId/suspend', adminAuth, riderController.suspendRider);

// Rider routes (requires rider authentication)
router.use(riderAuth);

router.get('/profile', riderController.getRiderProfile);
router.put('/profile', riderController.updateRiderProfile);
router.patch('/status', riderController.updateStatus);
router.patch('/location', riderController.updateLocation);

router.get('/available-orders', riderController.getAvailableOrders);
router.post('/orders/:orderId/accept', riderController.acceptOrder);
router.patch('/orders/:orderId/status', riderController.updateOrderStatus);

router.get('/earnings', riderController.getEarnings);
router.get('/delivery-history', riderController.getDeliveryHistory);
router.get('/stats', riderController.getRiderStats);

module.exports = router;
