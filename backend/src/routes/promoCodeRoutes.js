const express = require('express');
const router = express.Router();
const promoCodeController = require('../controllers/promoCodeController');
const { protect, authorize } = require('../middleware/auth');
const { body } = require('express-validator');
const validate = require('../middleware/validator');

// User routes
router.use(protect);

router.get('/active', promoCodeController.getActivePromoCodes);
router.post(
  '/validate',
  [
    body('code').notEmpty().withMessage('Promo code is required'),
    body('orderAmount').isNumeric().withMessage('Order amount must be a number'),
    validate,
  ],
  promoCodeController.validatePromoCode
);

// Admin routes
router.use(authorize('admin'));

router.post(
  '/',
  [
    body('code').notEmpty().withMessage('Code is required'),
    body('type').isIn(['percentage', 'fixed']).withMessage('Invalid type'),
    body('value').isNumeric().withMessage('Value must be a number'),
    body('validFrom').isISO8601().withMessage('Valid from date is required'),
    body('validUntil').isISO8601().withMessage('Valid until date is required'),
    validate,
  ],
  promoCodeController.createPromoCode
);

router.get('/', promoCodeController.getAllPromoCodes);
router.get('/:promoId', promoCodeController.getPromoCodeById);
router.get('/:promoId/stats', promoCodeController.getPromoCodeStats);
router.put('/:promoId', promoCodeController.updatePromoCode);
router.patch('/:promoId/toggle-status', promoCodeController.togglePromoCodeStatus);
router.delete('/:promoId', promoCodeController.deletePromoCode);

module.exports = router;
