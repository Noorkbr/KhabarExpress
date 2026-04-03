const express = require('express');
const router = express.Router();
const riderController = require('../controllers/riderController');
const { protect, authorize } = require('../middleware/auth');
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

// Rider routes (requires rider authentication)
router.use(protect);
router.use(authorize('rider'));

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

// Admin routes
router.get('/', authorize('admin'), riderController.getAllRiders);
router.patch('/:riderId/approve', authorize('admin'), riderController.approveRider);
router.patch('/:riderId/suspend', authorize('admin'), riderController.suspendRider);

module.exports = router;
