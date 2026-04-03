const multer = require('multer');
const path = require('path');
const fs = require('fs');

// Ensure upload directories exist
const uploadDirs = {
  restaurants: 'uploads/restaurants',
  menu: 'uploads/menu',
  users: 'uploads/users',
  riders: 'uploads/riders',
  deliveries: 'uploads/deliveries',
};

Object.values(uploadDirs).forEach(dir => {
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }
});

// Configure storage
const storage = multer.diskStorage({
  destination: function (req, file, cb) {
    const uploadType = req.uploadType || 'general';
    const dir = uploadDirs[uploadType] || 'uploads/general';
    cb(null, dir);
  },
  filename: function (req, file, cb) {
    const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
    cb(null, file.fieldname + '-' + uniqueSuffix + path.extname(file.originalname));
  }
});

// File filter
const fileFilter = (req, file, cb) => {
  // Accept images only
  const allowedMimeTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
  const allowedExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.webp'];
  
  const ext = path.extname(file.originalname).toLowerCase();
  
  if (allowedMimeTypes.includes(file.mimetype) && allowedExtensions.includes(ext)) {
    cb(null, true);
  } else {
    cb(new Error('Only image files (jpg, jpeg, png, gif, webp) are allowed!'), false);
  }
};

// Create multer upload instance
const upload = multer({
  storage: storage,
  limits: {
    fileSize: 5 * 1024 * 1024, // 5MB limit
  },
  fileFilter: fileFilter,
});

// Different upload configurations
const uploadConfigs = {
  single: (fieldName, uploadType) => {
    return (req, res, next) => {
      req.uploadType = uploadType;
      upload.single(fieldName)(req, res, next);
    };
  },
  
  multiple: (fieldName, maxCount, uploadType) => {
    return (req, res, next) => {
      req.uploadType = uploadType;
      upload.array(fieldName, maxCount)(req, res, next);
    };
  },
  
  fields: (fields, uploadType) => {
    return (req, res, next) => {
      req.uploadType = uploadType;
      upload.fields(fields)(req, res, next);
    };
  },
};

module.exports = uploadConfigs;
