const mongoose = require('mongoose');

const settingSchema = new mongoose.Schema({
  key: {
    type: String,
    default: 'platform',
    unique: true,
  },
  appName: {
    type: String,
    default: 'KhabarExpress',
  },
  deliveryFee: {
    type: Number,
    default: 20,
    min: 0,
  },
  adminProfitRate: {
    type: Number,
    default: 5,
    min: 0,
    max: 100,
  },
  minOrderAmount: {
    type: Number,
    default: 50,
    min: 0,
  },
  maintenanceMode: {
    type: Boolean,
    default: false,
  },
  paymentMethods: {
    bkash: { type: Boolean, default: true },
    rocket: { type: Boolean, default: true },
    upay: { type: Boolean, default: true },
    sslcommerz: { type: Boolean, default: true },
    cod: { type: Boolean, default: true },
  },
}, {
  timestamps: true,
});

module.exports = mongoose.model('Setting', settingSchema);
