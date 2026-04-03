const express = require('express');
const router = express.Router();
const menuItemController = require('../controllers/menuItemController');
const { restaurantAuth } = require('../middleware/auth');

// Public routes
router.get('/restaurant/:restaurantId', menuItemController.getRestaurantMenuItems);
router.get('/:id', menuItemController.getMenuItem);

// Restaurant owner routes
router.post('/', restaurantAuth, menuItemController.createMenuItem);
router.put('/:id', restaurantAuth, menuItemController.updateMenuItem);
router.patch('/:id/availability', restaurantAuth, menuItemController.toggleAvailability);
router.delete('/:id', restaurantAuth, menuItemController.deleteMenuItem);

module.exports = router;
