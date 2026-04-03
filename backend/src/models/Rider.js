const mongoose = require('mongoose');

const riderSchema = new mongoose.Schema({
  name: {
    type: String,
    required: true,
  },
  phone: {
    type: String,
    required: true,
    unique: true,
    match: /^\+880\d{10}$/,
  },
  email: String,
  profileImage: String,
  nid: String, // National ID
  drivingLicense: String,
  vehicleType: {
    type: String,
    enum: ['motorcycle', 'bicycle', 'car'],
    default: 'motorcycle',
  },
  vehicleNumber: String,
  currentLocation: {
    type: {
      type: String,
      enum: ['Point'],
      default: 'Point',
    },
    coordinates: [Number],
  },
  zone: String, // Gulshan, Dhanmondi, etc.
  status: {
    type: String,
    enum: ['offline', 'available', 'busy', 'on_break'],
    default: 'offline',
  },
  rating: {
    type: Number,
    default: 0,
    min: 0,
    max: 5,
  },
  totalReviews: {
    type: Number,
    default: 0,
  },
  totalDeliveries: {
    type: Number,
    default: 0,
  },
  earnings: {
    total: {
      type: Number,
      default: 0,
    },
    thisWeek: {
      type: Number,
      default: 0,
    },
    thisMonth: {
      type: Number,
      default: 0,
    },
  },
  bankDetails: {
    accountName: String,
    accountNumber: String,
    bankName: String,
    branchName: String,
    bkashNumber: String,
    nagadNumber: String,
  },
  isActive: {
    type: Boolean,
    default: true,
  },
}, {
  timestamps: true,
});

riderSchema.index({ currentLocation: '2dsphere' });
riderSchema.index({ status: 1, zone: 1 });

module.exports = mongoose.model('Rider', riderSchema);
