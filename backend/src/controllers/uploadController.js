const path = require('path');
const fs = require('fs');

// Upload single image
exports.uploadImage = async (req, res, next) => {
  try {
    if (!req.file) {
      return res.status(400).json({
        success: false,
        message: 'No file uploaded',
      });
    }

    const fileUrl = `/uploads/${path.basename(path.dirname(req.file.path))}/${req.file.filename}`;

    res.json({
      success: true,
      message: 'File uploaded successfully',
      data: {
        filename: req.file.filename,
        originalName: req.file.originalname,
        size: req.file.size,
        mimetype: req.file.mimetype,
        url: fileUrl,
      },
    });
  } catch (error) {
    next(error);
  }
};

// Upload multiple images
exports.uploadMultipleImages = async (req, res, next) => {
  try {
    if (!req.files || req.files.length === 0) {
      return res.status(400).json({
        success: false,
        message: 'No files uploaded',
      });
    }

    const files = req.files.map(file => ({
      filename: file.filename,
      originalName: file.originalname,
      size: file.size,
      mimetype: file.mimetype,
      url: `/uploads/${path.basename(path.dirname(file.path))}/${file.filename}`,
    }));

    res.json({
      success: true,
      message: 'Files uploaded successfully',
      data: {
        count: files.length,
        files,
      },
    });
  } catch (error) {
    next(error);
  }
};

// Delete uploaded file
exports.deleteFile = async (req, res, next) => {
  try {
    const { filename } = req.params;
    const { type } = req.query;

    // Construct file path
    const filePath = path.join(__dirname, '../../uploads', type || 'general', filename);

    // Check if file exists
    if (!fs.existsSync(filePath)) {
      return res.status(404).json({
        success: false,
        message: 'File not found',
      });
    }

    // Delete file
    fs.unlinkSync(filePath);

    res.json({
      success: true,
      message: 'File deleted successfully',
    });
  } catch (error) {
    next(error);
  }
};

// Get uploaded file info
exports.getFileInfo = async (req, res, next) => {
  try {
    const { filename } = req.params;
    const { type } = req.query;

    const filePath = path.join(__dirname, '../../uploads', type || 'general', filename);

    if (!fs.existsSync(filePath)) {
      return res.status(404).json({
        success: false,
        message: 'File not found',
      });
    }

    const stats = fs.statSync(filePath);

    res.json({
      success: true,
      data: {
        filename,
        size: stats.size,
        createdAt: stats.birthtime,
        modifiedAt: stats.mtime,
      },
    });
  } catch (error) {
    next(error);
  }
};

module.exports = exports;
