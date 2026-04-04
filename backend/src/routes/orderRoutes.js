const express = require('express');
const router = express.Router();
const { body } = require('express-validator');
const orderController = require('../controllers/orderController');
const { auth, restaurantAuth } = require('../middleware/auth');
const validate = require('../middleware/validator');

// All order routes require authentication
router.use(auth);

// Create order
router.post('/',
  [
    body('restaurantId').notEmpty().withMessage('Restaurant ID is required'),
    body('items').isArray({ min: 1 }).withMessage('Order must have at least one item'),
    body('deliveryAddress').notEmpty().withMessage('Delivery address is required'),
    body('paymentMethod')
      .isIn(['bkash', 'nagad', 'rocket', 'card', 'cod'])
      .withMessage('Invalid payment method'),
  ],
  validate,
  orderController.createOrder
);

// Get user orders
router.get('/', orderController.getUserOrders);

// Restaurant order management routes
router.get('/restaurant/my', restaurantAuth, orderController.getRestaurantOrders);
router.patch('/:id/accept', restaurantAuth, orderController.acceptOrder);
router.patch('/:id/reject',
  restaurantAuth,
  [
    body('reason').optional().isString().withMessage('Reason must be a string'),
  ],
  validate,
  orderController.rejectOrder
);
router.patch('/:id/status',
  restaurantAuth,
  [
    body('status')
      .isIn(['preparing', 'ready', 'picked_up'])
      .withMessage('Invalid status'),
  ],
  validate,
  orderController.updateOrderStatus
);

// Get order by ID
router.get('/:id', orderController.getOrderById);

// Track order
router.get('/:id/track', orderController.trackOrder);

// Cancel order
router.patch('/:id/cancel',
  [
    body('reason').notEmpty().withMessage('Cancellation reason is required'),
  ],
  validate,
  orderController.cancelOrder
);

module.exports = router;
