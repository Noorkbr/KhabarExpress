const express = require('express');
const router = express.Router();
const zoneController = require('../controllers/zoneController');
const { adminAuth } = require('../middleware/auth');

// Public routes
router.get('/', zoneController.getActiveZones);
router.get('/:id', zoneController.getZoneById);
router.get('/:id/restaurants', zoneController.getZoneRestaurants);

// Admin routes
router.get('/admin/all', adminAuth, zoneController.getAllZones);
router.post('/', adminAuth, zoneController.createZone);
router.put('/:id', adminAuth, zoneController.updateZone);
router.patch('/:id/toggle', adminAuth, zoneController.toggleZoneActive);
router.delete('/:id', adminAuth, zoneController.deleteZone);

module.exports = router;
