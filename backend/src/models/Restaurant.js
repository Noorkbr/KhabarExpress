const mongoose = require('mongoose');

const restaurantSchema = new mongoose.Schema({
  name: {
    type: String,
    required: true,
  },
  nameBn: String, // Bangla name
  description: String,
  descriptionBn: String,
  coverImage: String,
  logo: String,
  phone: {
    type: String,
    required: true,
  },
  email: String,
  cuisines: [String], // ['Bengali', 'Chinese', 'Fast Food']
  category: {
    type: String,
    enum: ['Restaurant', 'Cloud Kitchen', 'Bakery', 'Cafe', 'Street Food'],
    default: 'Restaurant',
  },
  location: {
    type: {
      type: String,
      enum: ['Point'],
      default: 'Point',
    },
    coordinates: {
      type: [Number], // [longitude, latitude]
      required: true,
    },
  },
  address: {
    street: String,
    area: String,
    thana: String,
    district: String,
    postalCode: String,
  },
  businessHours: [{
    day: {
      type: String,
      enum: ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'],
    },
    openTime: String, // "09:00"
    closeTime: String, // "23:00"
    isClosed: Boolean,
  }],
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
  deliveryTime: {
    min: Number, // minutes
    max: Number,
  },
  minOrderAmount: {
    type: Number,
    default: 0, // in BDT paisa
  },
  deliveryRadius: {
    type: Number,
    default: 5, // in km
  },
  deliveryZones: [{
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Zone',
  }],
  commission: {
    type: Number,
    default: 18, // percentage
  },
  documents: {
    tradeLicense: String,
    nidOwner: String,
    bankDetails: String,
  },
  payoutMethod: {
    type: {
      type: String,
      enum: ['bkash', 'nagad', 'bank'],
      default: 'bkash',
    },
    accountNumber: String,
    accountName: String,
    bankName: String,
    branchName: String,
  },
  approvalStatus: {
    type: String,
    enum: ['pending', 'approved', 'rejected'],
    default: 'pending',
  },
  rejectionReason: String,
  isActive: {
    type: Boolean,
    default: true,
  },
  isOpen: {
    type: Boolean,
    default: false,
  },
  featured: {
    type: Boolean,
    default: false,
  },
  totalOrders: {
    type: Number,
    default: 0,
  },
}, {
  timestamps: true,
});

// Index for geospatial queries
restaurantSchema.index({ location: '2dsphere' });
restaurantSchema.index({ cuisines: 1 });
restaurantSchema.index({ rating: -1 });

module.exports = mongoose.model('Restaurant', restaurantSchema);
