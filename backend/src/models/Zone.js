const mongoose = require('mongoose');

const zoneSchema = new mongoose.Schema({
  name: {
    type: String,
    required: true,
  },
  nameBn: {
    type: String,
    required: true,
  },
  polygon: {
    type: {
      type: String,
      enum: ['Polygon'],
      default: 'Polygon',
    },
    coordinates: {
      type: [[[Number]]], // Array of arrays of coordinate pairs
      required: true,
    },
  },
  center: {
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
  deliveryFee: {
    type: Number,
    required: true,
    default: 3000, // in paisa (৳30)
  },
  perKmFee: {
    type: Number,
    default: 800, // in paisa (৳8)
  },
  estimatedTime: {
    type: String,
    default: '30-45 min',
  },
  isActive: {
    type: Boolean,
    default: true,
  },
  restaurantCount: {
    type: Number,
    default: 0,
  },
  riderCount: {
    type: Number,
    default: 0,
  },
  createdAt: {
    type: Date,
    default: Date.now,
  },
  updatedAt: {
    type: Date,
    default: Date.now,
  },
}, {
  timestamps: true,
});

// Index for geospatial queries
zoneSchema.index({ polygon: '2dsphere' });
zoneSchema.index({ center: '2dsphere' });

module.exports = mongoose.model('Zone', zoneSchema);
