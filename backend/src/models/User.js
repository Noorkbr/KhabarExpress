const mongoose = require('mongoose');

const addressSchema = new mongoose.Schema({
  label: {
    type: String,
    enum: ['Home', 'Office', 'Other'],
    default: 'Home',
  },
  houseNo: String,
  roadNo: String,
  area: String,
  thana: String,
  district: String,
  division: String,
  postalCode: String,
  landmark: String,
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
  isDefault: {
    type: Boolean,
    default: false,
  },
});

const userSchema = new mongoose.Schema({
  phone: {
    type: String,
    required: true,
    unique: true,
    match: /^\+880\d{10}$/, // Bangladesh phone format
  },
  name: {
    type: String,
    required: true,
  },
  nameBn: String, // Bangla name
  email: {
    type: String,
    sparse: true,
    lowercase: true,
  },
  profileImage: String,
  addresses: [addressSchema],
  favorites: [{
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Restaurant',
  }],
  preferredLanguage: {
    type: String,
    enum: ['en', 'bn'],
    default: 'en',
  },
  isVerified: {
    type: Boolean,
    default: false,
  },
  role: {
    type: String,
    enum: ['customer', 'admin'],
    default: 'customer',
  },
}, {
  timestamps: true,
});

// Index for geospatial queries
userSchema.index({ 'addresses.location': '2dsphere' });

module.exports = mongoose.model('User', userSchema);
