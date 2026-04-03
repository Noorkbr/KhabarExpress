const express = require('express');
const router = express.Router();
const categoryController = require('../controllers/categoryController');
const { restaurantAuth } = require('../middleware/auth');

// Public routes
router.get('/restaurant/:restaurantId', categoryController.getRestaurantCategories);

// Restaurant owner routes
router.post('/', restaurantAuth, categoryController.createCategory);
router.put('/:id', restaurantAuth, categoryController.updateCategory);
router.delete('/:id', restaurantAuth, categoryController.deleteCategory);
router.post('/reorder', restaurantAuth, categoryController.reorderCategories);

module.exports = router;
