const express = require('express');
const router = express.Router();
const uploadController = require('../controllers/uploadController');
const upload = require('../middleware/upload');
const { protect } = require('../middleware/auth');

// All upload routes require authentication
router.use(protect);

// Restaurant images
router.post(
  '/restaurant',
  upload.single('image', 'restaurants'),
  uploadController.uploadImage
);

router.post(
  '/restaurant/multiple',
  upload.multiple('images', 5, 'restaurants'),
  uploadController.uploadMultipleImages
);

// Menu item images
router.post(
  '/menu',
  upload.single('image', 'menu'),
  uploadController.uploadImage
);

// User profile images
router.post(
  '/profile',
  upload.single('image', 'users'),
  uploadController.uploadImage
);

// Rider documents and images
router.post(
  '/rider/document',
  upload.single('document', 'riders'),
  uploadController.uploadImage
);

// Delivery proof images
router.post(
  '/delivery/proof',
  upload.single('proof', 'deliveries'),
  uploadController.uploadImage
);

// File management
router.delete('/:filename', uploadController.deleteFile);
router.get('/:filename/info', uploadController.getFileInfo);

module.exports = router;
