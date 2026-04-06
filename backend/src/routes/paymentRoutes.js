const express = require('express');
const router = express.Router();
const paymentController = require('../controllers/paymentController');
const { protect, authorize } = require('../middleware/auth');
const { body } = require('express-validator');
const validate = require('../middleware/validator');
const { paymentLimiter } = require('../middleware/rateLimiter');

// User routes (requires authentication)
router.use(protect);

router.post(
  '/create',
  paymentLimiter,
  [
    body('orderId').notEmpty().withMessage('Order ID is required'),
    body('method').isIn(['bkash', 'nagad', 'rocket', 'upay', 'card', 'cod']).withMessage('Invalid payment method'),
    validate,
  ],
  paymentController.createPayment
);

router.get('/order/:orderId', paymentController.getPaymentByOrderId);
router.get('/history', paymentController.getPaymentHistory);

// Callback routes (public - called by payment gateways)
router.post('/bkash/callback', paymentController.bkashCallback);
router.post('/nagad/callback', paymentController.nagadCallback);
router.post('/sslcommerz/callback', paymentController.sslCommerzCallback);
router.post('/rocket/callback', paymentController.rocketCallback);
router.post('/upay/callback', paymentController.upayCallback);

// Admin routes
router.get('/', authorize('admin'), paymentController.getAllPayments);
router.post('/:paymentId/refund', authorize('admin'), paymentController.refundPayment);

module.exports = router;
