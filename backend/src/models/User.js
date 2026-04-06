const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');

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
  password: {
    type: String,
    select: false, // Don't return password by default
  },
  isBanned: {
    type: Boolean,
    default: false,
  },
}, {
  timestamps: true,
});

// Index for geospatial queries
userSchema.index({ 'addresses.location': '2dsphere' });

// Hash password before save (only for admin users with password)
userSchema.pre('save', async function (next) {
  if (!this.isModified('password') || !this.password) return next();
  this.password = await bcrypt.hash(this.password, 12);
  next();
});

// Compare password method
userSchema.methods.comparePassword = async function (candidatePassword) {
  return bcrypt.compare(candidatePassword, this.password);
};

module.exports = mongoose.model('User', userSchema);
