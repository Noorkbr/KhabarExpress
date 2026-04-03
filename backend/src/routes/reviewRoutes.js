const express = require('express');
const router = express.Router();
const reviewController = require('../controllers/reviewController');
const { protect, authorize } = require('../middleware/auth');
const { body } = require('express-validator');
const validate = require('../middleware/validator');

// Public routes
router.get('/restaurant/:restaurantId', reviewController.getRestaurantReviews);

// User routes (requires authentication)
router.use(protect);

router.post(
  '/',
  [
    body('orderId').notEmpty().withMessage('Order ID is required'),
    body('foodRating').isInt({ min: 1, max: 5 }).withMessage('Food rating must be between 1 and 5'),
    body('deliveryRating').optional().isInt({ min: 1, max: 5 }).withMessage('Delivery rating must be between 1 and 5'),
    validate,
  ],
  reviewController.createReview
);

router.get('/my-reviews', reviewController.getUserReviews);
router.put('/:reviewId', reviewController.updateReview);
router.delete('/:reviewId', reviewController.deleteReview);

// Restaurant owner routes
router.post('/:reviewId/respond', authorize('restaurant'), reviewController.respondToReview);

// Admin routes
router.get('/', authorize('admin'), reviewController.getAllReviews);
router.patch('/:reviewId/toggle-visibility', authorize('admin'), reviewController.toggleReviewVisibility);

module.exports = router;
