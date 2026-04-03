const mongoose = require('mongoose');

const promoCodeSchema = new mongoose.Schema({
  code: {
    type: String,
    required: true,
    unique: true,
    uppercase: true,
  },
  description: String,
  descriptionBn: String,
  type: {
    type: String,
    enum: ['percentage', 'fixed'],
    required: true,
  },
  value: {
    type: Number,
    required: true, // percentage or fixed amount in paisa
  },
  minOrderAmount: {
    type: Number,
    default: 0,
  },
  maxDiscount: Number, // max discount in paisa (for percentage type)
  usageLimit: {
    total: Number,
    perUser: {
      type: Number,
      default: 1,
    },
  },
  usageCount: {
    type: Number,
    default: 0,
  },
  validFrom: {
    type: Date,
    required: true,
  },
  validUntil: {
    type: Date,
    required: true,
  },
  applicableTo: {
    type: String,
    enum: ['all', 'first_order', 'specific_restaurants'],
    default: 'all',
  },
  restaurants: [{
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Restaurant',
  }],
  isActive: {
    type: Boolean,
    default: true,
  },
}, {
  timestamps: true,
});

promoCodeSchema.index({ code: 1 });
promoCodeSchema.index({ validFrom: 1, validUntil: 1 });

module.exports = mongoose.model('PromoCode', promoCodeSchema);
